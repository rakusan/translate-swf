// throw operator;

function checkAddress(url) 
{
    if (url.indexOf("http:") != 0) 
    {
        throw new Error("Invalid URL");
    }
}

url = "www.flagstonesoftware.com";

try
{
    checkAddress(url);
}
catch (Error e)
{
    e.toString();
}
finally 
{
    url = "http://" + url; 
}
