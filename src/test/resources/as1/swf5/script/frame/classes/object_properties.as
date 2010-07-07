// Object properties


a = new Object();

x = a.constructor;
x = a.__proto__;

a.__resolve = function (name) 
{
    trace("Resolving "+name);
};

