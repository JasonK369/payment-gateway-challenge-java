# Take home exercise documentation

## Assumption

- API calling between a safe connection / environment, so security is no needed
- People who look at the log will not expose the content to outsider, so some of the log are not masked (if there is personal information)


## Design consideration

### Separation of functionality
I tried to separate the task into different type of classes, for example:
- controller: responsible for exposing endpoint
- util: act as a library
- service: contains business

By such separation, we can add new feature without modify / introduce less modification to other unrelated classes
Also we can find purpose of the class quickly

### Error handling
For example when sending payment request, we have to take extra care on didn't sending request more than once (unless we are told to do so)
Otherwise it may cause monetary losses for our user

So it is important to tell user the status when there is any error
Such as providing status information(eg: `Authorized`, `Declined` and `Rejected`).

Also, it is better to have the reason / support message for different status, especially for anomalous one

And in most of the time, payment gateway (i.e.: this system) should not retry any critical API call for the user, they should retry by themselves

On the other hand, I've creating distinct, granular exception classes allows for more precise error handling. This can allow us to have targeted error handling (eg: create different catch block for different types of exceptions) 

### Using annotation
In Spring and Java family has variety of annotation (I believe other language / framework also have them)

where we can use them to help implementation, for example, for the request classes, I've used different annotation to validate different fields

which save us time to implement a logic for validation and it is very simple to read




