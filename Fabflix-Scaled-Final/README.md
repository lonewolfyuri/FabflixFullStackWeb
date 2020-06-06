# Fabflix by George Gabricht
## cs122b-spring20-team-141
## Website: https://ec2-18-216-126-43.us-east-2.compute.amazonaws.com:8443/Fabflix/

- # General
    - #### Team#: 141
    
    - #### Names: George Gabricht
    
    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment: 


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    
    - #### Explain how Connection Pooling works with two backend SQL.
    

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

    - #### How read/write requests were routed to Master/Slave SQL?
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

### =======================================================================

## Project 4

## I have set up:

  Full Text Search for more accurate results
  
  Autocomplete for faster results
  
  Android Mobile App to facilitate mobile users

## Video Submission URL: https://youtu.be/e-phJlJkOmY

### =======================================================================

## Project 3

## I have set up:

  Recaptcha and Prepared Statements
  
  HTTPS and SSL Certificates
  
  Admin Portal with Metadata, Stored Procedures and XML Parser
  
  Performance Optimizations
  
  Additional Security Features
  
## Optimizations Report

  Initially, my XML Parser ran very slowly, often taking roughly 25 minutes to complete parsing and updating the DB. With these optimizations, my performance severely improved, resulting in an XML Parsing and DB updating time of 2-3 Minutes.
  
  I have Implemented the following optimizations:
  
    1) HashMaps are used extensively throughout the Document Parsing process to efficiently check for uniqueness,
    ensuring no duplicates are entered in the DB and allowing us to retrieve values when needed. This first 
    improvement granted me a huge speed increase, from 25 minutes to 10 minutes.
    
    2) Batch Processing of SQL Insert Statements to minimize the number of Stored Procedure calls to update the 
    DB. Knowing that all of our values in the hashmaps are unique, due to optimization 1, we are able to simply 
    insert each new entry, without checking for existence in the DB. This granted me a large chunk of speed also, 
    bringing my running speed form 10 minutes to about 2-3 minutes.

## Video Submission URL: https://youtu.be/_wBcdAXt5xI

### =======================================================================

## Project 2

## I have set up:

  Login Filter and Servlet
  
  Browsing, Searching and Filtering/Ordering of Movies as well as Stars.
  
  Http Sessions that Include Search History, Shopping Cart and More.
  
  Checkout and Order Confirmation
  
  All requirements of Assignment met / surpassed.
  
## Video Submission URL: https://youtu.be/3iDAdM4nZP0

### =======================================================================

## Project 1

## I have set up: 
  
  MySQL Database named 'moviedb'
  
  A fleet of Java Servlets hosted through tomcat
  
  An AWS EC2 Instance
  
  A series of HTML/CSS/JS files to serve as a Front End

## Video Submission URL: https://youtu.be/LkGPFNQ9Cr0

cs122b-spring20-team-141 created by GitHub Classroom
