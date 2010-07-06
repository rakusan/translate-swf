// create a new local connection

conn = new LocalConnection();

// allow domains to create connections - flash 6

conn.allowDomain = function(domain)
{
    return(domain==this.domain());
};
