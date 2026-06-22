from tenacity import retry, stop_after_attempt

def retry_policy():
    return retry(stop=stop_after_attempt(3))