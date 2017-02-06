

def time_delta_limited_count(
        from_step,
        to_step,
        chain,
        period,
        limit,
        time_field_name = '_recorded_start_time',
        **kwargs
):
    assert(0 <= from_step < to_step)
    return 'create embedded sensor', {
        'embed': [
            {
                'step': from_step,
                'dependency': 'save timestamp receiver'
            },
            {
                'step': to_step,
                'dependency': 'embedded sensor receiver'
            }
        ],
        'chain': chain,
        'args': {
            'period': period,
            'strategy': 'embedded sensor time delta strategy for limited count',
            'timeFieldName': time_field_name,
            'limit': limit
        }
    }

TEMPLATES = {
    'time_delta_limited_count': time_delta_limited_count
}
