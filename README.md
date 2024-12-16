# How to run
1. compile and run either(non-threaded or multi-threaded) java file on terminal (server terminal)
   
2. On a new terminal(client) run:

       curl -x http://localhost:8888 http://example.com

3. Observe changes in server terminal by repeating the above curl command, you can modify URL, etc.
4. Any improvements- branch out and open a Pull Request for merging it!
   
# Sample Results:

JMeter Configurations:

number of threads(users): 10

Ramp up period: 10 seconds

Loop count: 5 loops per thread

HTTP method: GET

Proxy: localhost

Port no.: 8888

1. Response time graph without threading or caching:<br>
   <img width="599" alt="Screenshot 2024-11-07 at 5 17 58 PM" src="https://github.com/user-attachments/assets/f27edf4b-3cfe-4545-9c51-6c87cf9415fc">

   With threading and caching:
   
   <img width="612" alt="Screenshot 2024-11-07 at 5 19 07 PM" src="https://github.com/user-attachments/assets/9884ad78-f8df-4a5a-8e76-9c23984f596c">

For detailed results: 
https://docs.google.com/document/d/1MOJmOYIRqkFzGRbfYFOorpbULVSE8_CXzx2xWmhbuXc/edit?usp=sharing
