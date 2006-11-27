// create a new local connection

conn = new LocalConnection();

// allow domains to create connections - flash 7

conn.allowDomain = function(domain) 
{
    allowed = false;
    
    allowed = allowed || domain == "www.flagstonesoftware.com";
    allowed = allowed || domain == "store.flagstonesoftware.com";
    
    return (allowed);
};


// allow insecure domains to create connections - flash 7

conn.allowInsecureDomain = function(domain) 
{
    allowed = false;
    
    allowed = allowed || domain == this.domain();
    allowed = allowed || domain == "www.sourceforge.net";
    
    return (allowed);
};