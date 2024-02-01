'use strict';

const ip4ToInt = ip =>
    ip.split('.').reduce((int, oct) => (int << 8) + parseInt(oct, 10), 0) >>> 0;

const isIp4InCidr = ip => cidr => {
    const [range, bits = 32] = cidr.split('/');
    const mask = ~(2 ** (32 - bits) - 1);
    return mask === 0
        ? ip4ToInt(ip) === ip4ToInt(range)
        : (ip4ToInt(ip) & mask) === (ip4ToInt(range) & mask);
};

const isIp4InCidrs = (ip, cidrs) => cidrs.some(isIp4InCidr(ip));

exports.handler = async(event) => {
    if (process.env.ENVIRONMENT === 'build') {
        return {
            'isAuthorized': true
        };
    }
    const ipAddress = event.requestContext.http.sourceIp;
    const validIps = [
        '217.196.229.77/32',
        '217.196.229.79/32',
        '217.196.229.80/31',
        '51.149.8.0/25',
        '51.149.8.128/29',
        '213.86.153.211/32',
        '213.86.153.212/31',
        '213.86.153.214/32',
        '213.86.153.235/32',
        '213.86.153.236/31',
        '213.86.153.231/32'];
    const isValidIp = isIp4InCidrs(ipAddress, validIps);
    return {
        'isAuthorized': isValidIp
    };
}