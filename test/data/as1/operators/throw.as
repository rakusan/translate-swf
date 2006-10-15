// throw operator;

function checkAddress(url) 
{
    if (url.indexOf("http:") != 0) 
    {
        throw new Error("Invalid URL");
    }
}