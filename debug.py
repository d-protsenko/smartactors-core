#!/usr/bin/env python2
import httplib
import code
import argparse
import urlparse
import json

# Globals

_BANNER='''
SmartActors debugger client console.
'''

_CONSOLE_SCOPE={}

_CONFIG={}

_CURRENT_SESSION_ID=None

_CONNECTION=None

_LAST_SESSIONS_LIST=[]

# Initialization & command execution

def parse_arguments():
    parser = argparse.ArgumentParser(description='Connect to SmartActors debugger actor.')
    parser.add_argument('--url', '-u', default='http://localhost:9909/', type=urlparse.urlparse, metavar='URL', dest='url',
                        help='URL of the server endpoint')
    parser.add_argument('--cchain', '-d', default='execute_debugger_command', type=str, metavar='chain', dest='command_chain',
                        help='name of the chain to use to send command messages to debugger')
    parser.add_argument('--chain', '-c', default=None, type=str, metavar='chain', dest='debuggable_chain',
                        help='chain to start debugging')
    parser.add_argument('--init', '-i', default=None, type=file, metavar='script', dest='init_script',
                        help='script to execute before start of interactive debugging')

    args = parser.parse_args()

    _CONFIG['server-address'] = args.url.netloc
    _CONFIG['server-path'] = args.url.path
    _CONFIG['command-chain'] = args.command_chain
    _CONFIG['debug-default-chain'] = args.debuggable_chain

    if args.init_script is not None:
        with args.init_script as f:
            _CONFIG['init-script'] = compile(f.read(), f.name, 'exec')

def open_connection():
    return httplib.HTTPConnection(_CONFIG['server-address'])

def execute_command(name, arg=None, session=True):
    session_id = None
    if session:
        if None is _CURRENT_SESSION_ID:
            raise Exception('No current session present.')
        session_id = _CURRENT_SESSION_ID

    body = json.dumps({
        'messageMapId': _CONFIG['command-chain'],
        'command': name,
        'args': arg,
        'sessionId': session_id
    })

    _CONNECTION.request('POST', _CONFIG['server-path'], body=body, headers={"Content-type": "application/json"})
    resp = json.loads(_CONNECTION.getresponse().read())

    if 'exception' in resp:
        raise Exception(exception_to_string(resp['exception']))

    return resp['result']

def init_console_functions():
    _CONSOLE_SCOPE['cmd'] = execute_command

    _CONSOLE_SCOPE['session'] = lambda: _CURRENT_SESSION_ID

    _CONSOLE_SCOPE['newSession'] = cmd_new_session
    _CONSOLE_SCOPE['state'] = cmd_show_state
    _CONSOLE_SCOPE['sessions'] = cmd_list_sessions
    _CONSOLE_SCOPE['connect'] = cmd_connect
    _CONSOLE_SCOPE['close'] = cmd_close_session

    _CONSOLE_SCOPE['setMessageField'] = cmd_set_message_field

    _CONSOLE_SCOPE['trace'] = cmd_stack_trace
    _CONSOLE_SCOPE['goTo'] = cmd_go_to

    _CONSOLE_SCOPE['setMessage'] = cmd_set_x('setMessage')
    _CONSOLE_SCOPE['setChain'] = cmd_set_x('setChain')
    _CONSOLE_SCOPE['setStackDepth'] = cmd_set_x('setStackDepth')
    _CONSOLE_SCOPE['setStepMode'] = cmd_set_x('stepMode')
    _CONSOLE_SCOPE['setBreakOnException'] = cmd_set_x('setBreakOnException')
    _CONSOLE_SCOPE['call'] = cmd_set_x('call')

    _CONSOLE_SCOPE['start'] = cmd_do_x('start')
    _CONSOLE_SCOPE['stop'] = cmd_do_x('stop')
    _CONSOLE_SCOPE['pause'] = cmd_do_x('pause')
    _CONSOLE_SCOPE['cont'] = cmd_do_x('continue')
    _CONSOLE_SCOPE['processException'] = cmd_do_x('processException')

    _CONSOLE_SCOPE['getMessage'] = cmd_get_x('getMessage')
    _CONSOLE_SCOPE['getStackTrace'] = cmd_get_x('getStackTrace')
    _CONSOLE_SCOPE['getException'] = cmd_get_x('getException')

    _CONSOLE_SCOPE['setBp'] = cmd_set_breakpoint
    _CONSOLE_SCOPE['listBp'] = cmd_list_breakpoints
    _CONSOLE_SCOPE['modBp'] = cmd_modify_breakpoint

# Output formatting

def exception_to_string(e):
    s = '{0}\nat {1}:{2}'.format(
        e['message'],
        e['stackTrace'][0]['className'],
        e['stackTrace'][0]['lineNumber'])

    if 'cause' in e and e['cause'] is not None:
        s += '\ncaused by: ' + exception_to_string(e['cause'])

    return s

def chain_step_to_string(s):
    if 'handler' in s:
        return '{0}#{1}'.format(s['target'], s['handler'])
    else:
        return s['target']

# Commands

def cmd_new_session():
    global _CURRENT_SESSION_ID
    _CURRENT_SESSION_ID = execute_command('newSession', session=False)
    print 'New session created:', _CURRENT_SESSION_ID

def cmd_show_state():
    isPaused = execute_command('isPaused')
    isRunning = execute_command('isRunning')
    isCompleted = execute_command('isCompleted')

    stackDepth = execute_command('getStackDepth')
    stepMode = execute_command('getStepMode')
    chainName = execute_command('getChainName') or '<not set>'
    breakOnException = execute_command('getBreakOnException')

    if (not isRunning) and (not isPaused):
        state = 'Not started'
    elif isRunning:
        state = 'Running'
    elif isPaused:
        state = 'Paused'
    else:
        state = 'In invalid state (!)'

    if isCompleted:
        state += ' (completed)'

    if stepMode < 0:
        stepMode = 'none'
    elif stepMode == 0x7fffffff:
        stepMode = 'all'

    print state, '; debugging chain:', str(chainName), '; step mode:', str(stepMode), '; on exception:',\
        'break' if breakOnException else 'handle', '; max stack depth:', str(stackDepth)

    if isPaused and (not isRunning):
        exception = execute_command('getException')

        if exception is None:
            exception = '<none>'
        else:
            exception = exception_to_string(exception)

        print 'Exception:', str(exception)

    if isPaused or not isRunning:
        message = execute_command('getMessage')

        if message is None:
            message = '<not set>'

        print 'Message content:', str(message)

def cmd_list_sessions():
    global _LAST_SESSIONS_LIST

    lst = execute_command('listSessions', session=False)

    for i in range(len(lst)):
        print '{0}) \t{1}'.format(i, lst[i])

    _LAST_SESSIONS_LIST = lst

def cmd_connect(id):
    global _LAST_SESSIONS_LIST, _CURRENT_SESSION_ID

    _CURRENT_SESSION_ID = _LAST_SESSIONS_LIST[id]

def cmd_close_session():
    global _CURRENT_SESSION_ID
    execute_command('closeSession', _CURRENT_SESSION_ID)
    _CURRENT_SESSION_ID = None

def cmd_set_message_field(fn, val, dep=None):
    print execute_command('setMessageField', {'name': fn, 'value': val, 'dependency': dep})

def cmd_get_x(c):
    def cmd():
        return execute_command(c)
    return cmd

def cmd_set_x(c):
    def cmd(value):
        print execute_command(c, value)
    return cmd

def cmd_do_x(c):
    def cmd():
        print execute_command(c)
    return cmd

def cmd_stack_trace(options=None):
    trace = execute_command('getStackTrace', options or {})

    for level in range(len(trace['chainsStack'])):
        chain = trace['chainsStack'][level]
        chainSteps = trace['chainsDump'][chain]['steps']
        currentStep = trace['stepsStack'][level]

        print 'Level', level, '; chain:', chain

        for step in range(len(chainSteps)):
            marker = ''
            marker += '-' if step <= currentStep else ' '
            marker += '>' if step == currentStep else ' '
            marker += ' '
            print '{2}{0}; {1})'.format(level, step, marker), chain_step_to_string(chainSteps[step])

def cmd_go_to(level, step):
    print execute_command('goTo', {'level': level, 'step': step})

def cmd_set_breakpoint(chain, step, enabled=True):
    print execute_command('setBreakpoint', {
        'chain': chain,
        'step': int(step),
        'enabled': enabled
    })

def cmd_list_breakpoints():
    lst = execute_command('listBreakpoints')

    for bp in lst:
        print '{0}\t{1})\tstep#{3} @ {2}\t'.format(
            '*' if bp['enabled'] else '-',
            bp['id'],
            bp['chain'],
            bp['step']
        ), chain_step_to_string(bp['args'])

def cmd_modify_breakpoint(bpId, enable=None):
    args = {'id': bpId}

    if enable is not None: args['enabled'] = enable

    print execute_command('modifyBreakpoint', args)

# Entry point

def launch_debugger():
    global _CONNECTION

    parse_arguments()

    _CONNECTION=open_connection()

    init_console_functions()

    if 'init-script' in _CONFIG:
        exec _CONFIG['init-script'] in _CONSOLE_SCOPE

    code.InteractiveConsole(locals=_CONSOLE_SCOPE).interact(_BANNER)

if __name__ == '__main__': launch_debugger()
