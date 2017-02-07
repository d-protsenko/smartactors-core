#!/usr/bin/env python2
import httplib
import code
import argparse
import urlparse
import json

from statistics_manager_templates.default_sensor_templates import TEMPLATES as DEFAULT_SENSOR_TEMPLATES
from statistics_manager_templates.default_collector_templates import TEMPLATES as DEFAULT_COLLECTOR_TEMPLATES

# Globals

_BANNER='''
SmartActors statistics manager client console.
'''

_IPYTHON_NOTICE='''
For better experience install the ipython:
# pip install ipython
'''

_CONSOLE_SCOPE={}

_CONFIG={}

_CONNECTION=None

_SENSOR_TEMPLATES={}

_COLLECTOR_TEMPLATES={}

# Initialization & command execution

def parse_arguments():
    parser = argparse.ArgumentParser(description='Connect to SmartActors statistics manager.')
    parser.add_argument('--url', '-u', default='http://localhost:9909/', type=urlparse.urlparse, metavar='URL', dest='url',
                        help='URL of the server endpoint')
    parser.add_argument('--cchain', '-d', default='execute_statistics_manager_command', type=str, metavar='chain', dest='command_chain',
                        help='name of the chain to use to send command messages to statistics manager')
    parser.add_argument('--init', '-i', default=None, type=file, metavar='script', dest='init_script',
                        help='script to execute before start of interactive statistics manager console')

    args = parser.parse_args()

    _CONFIG['server-address'] = args.url.netloc
    _CONFIG['server-path'] = args.url.path
    _CONFIG['command-chain'] = args.command_chain

    if args.init_script is not None:
        with args.init_script as f:
            _CONFIG['init-script'] = compile(f.read(), f.name, 'exec')

def open_connection():
    return httplib.HTTPConnection(_CONFIG['server-address'])

def send_request(message, chain):
    message = message or {}
    message['messageMapId'] = chain

    body = json.dumps(message)

    _CONNECTION.request('POST', _CONFIG['server-path'], body=body, headers={"Content-type": "application/json"})
    resp = _CONNECTION.getresponse().read()
    resp = json.loads(resp)

    if 'exception' in resp:
        raise Exception(exception_to_string(resp['exception']))

    return resp

def execute_command(name, arg=None):
    return send_request({
        'command': name,
        'args': arg
    }, _CONFIG['command-chain'])['result']

def init_console_functions():
    _CONSOLE_SCOPE['SENSOR_TEMPLATES'] = _SENSOR_TEMPLATES
    _CONSOLE_SCOPE['COLLECTOR_TEMPLATES'] = _COLLECTOR_TEMPLATES

    _CONSOLE_SCOPE['cmd'] = execute_command

    _CONSOLE_SCOPE['createSensor'] = cmd_create_sensor
    _CONSOLE_SCOPE['shutdownSensor'] = cmd_shutdown_sensor
    _CONSOLE_SCOPE['enumSensors'] = cmd_enum_sensors

    _CONSOLE_SCOPE['createCollector'] = cmd_create_collector
    _CONSOLE_SCOPE['enumCollectors'] = cmd_enum_collectors

    _CONSOLE_SCOPE['link'] = cmd_link
    _CONSOLE_SCOPE['unlink'] = cmd_unlink
    _CONSOLE_SCOPE['enumLinks'] = cmd_enum_links

    _CONSOLE_SCOPE['query'] = cmd_query

# Output formatting

def exception_to_string(e):
    s = '{0}\n\tat {1}:{2}'.format(
        e['message'],
        e['stackTrace'][0]['className'],
        e['stackTrace'][0]['lineNumber'])

    if 'cause' in e and e['cause'] is not None:
        s += '\n\n\tcaused by: ' + exception_to_string(e['cause'])

    return s

# Commands

def cmd_create_sensor(sensor_id, template_name, **kwargs):
    dependency, args = _SENSOR_TEMPLATES[template_name](**kwargs)
    return execute_command('createSensor', {
        'id': sensor_id,
        'dependency': dependency,
        'args': args
    })

def cmd_shutdown_sensor(sensor_id):
    return execute_command('shutdownSensor', sensor_id)

def cmd_create_collector(collector_id, template_name, **kwargs):
    args, query_step_config = _COLLECTOR_TEMPLATES[template_name](**kwargs)
    return execute_command('createCollector', {
        'id': collector_id,
        'args': args,
        'queryStepConfig': query_step_config
    })

def cmd_link(sensor_id, collector_id, step_args=None):
    return execute_command('link', {
        'sensor': sensor_id,
        'collector': collector_id,
        'stepConfig': step_args or {}
    })

def cmd_unlink(sensor_id, collector_id):
    return execute_command('unlink', {
        'sensor': sensor_id,
        'collector': collector_id
    })

def cmd_enum_sensors():
    return execute_command('enumSensors', None)

def cmd_enum_collectors():
    return execute_command('enumCollectors', None)

def cmd_query(collector_id, message=None):
    q_chain = execute_command('getCollectorQueryChain', collector_id)
    return send_request(message, q_chain)

def cmd_enum_links():
    return execute_command('enumLinks')

# Entry point

def launch_console():
    global _CONNECTION
    global _SENSOR_TEMPLATES
    global _COLLECTOR_TEMPLATES

    parse_arguments()

    _CONNECTION=open_connection()

    _SENSOR_TEMPLATES = dict(DEFAULT_SENSOR_TEMPLATES)

    _COLLECTOR_TEMPLATES = dict(DEFAULT_COLLECTOR_TEMPLATES)

    init_console_functions()

    if 'init-script' in _CONFIG:
        exec _CONFIG['init-script'] in _CONSOLE_SCOPE

    try:
        import IPython
        print _BANNER
        IPython.start_ipython(user_ns=_CONSOLE_SCOPE)
    except ImportError as e:
        code.InteractiveConsole(locals=_CONSOLE_SCOPE).interact(_BANNER+_IPYTHON_NOTICE)

if __name__ == '__main__': launch_console()
