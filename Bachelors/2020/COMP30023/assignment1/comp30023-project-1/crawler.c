/* Assignment 1 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948

 */

/* ========================================================================== */

/* Libraries to include and hash-defined variables sorted alphabetically */
/* -------------------------------------------------------------------- */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <regex.h>

#define PORT 80
#define BUFFER MAX_CHARACTERS
#define HEADER_SIZE 512
#define MAX_BYTES 100000
#define MAX_CHARACTERS 256
#define MAX_URLS 100

#define HTTP "(^http://.*)|(^HTTP://.*)"
#define PATH "/(.*/*.*)*/"
#define FILE "/.*$"
#define ANCHOR "(<a |<A )"
#define HREF "(href=\"|HREF=\")"
#define END_HREF "\""
#define FIRST_COMPONENT ".*\\."
#define NULL_BYTE "\0"


/* ========================================================================== */

/* Type Definitions */
/* --------------- */

// To hold the values needed to define a url

struct url{

    char host[MAX_CHARACTERS];
    char path[MAX_CHARACTERS];
    char file[MAX_CHARACTERS];

};


/* ========================================================================== */

/* Function prototypes made by me */
/* ------------------------------ */

int create_socket(char *hostname);

struct url categorise(char* url);

void GET_request(struct url url, char header[HEADER_SIZE]);

void parse_html(char *buffer,struct url *url_list,int *added);

void crawler(char* initial_url);


/* ========================================================================== */

/* Main function */
/* ------------- */

int main(int argc, char ** argv){
    
    crawler(argv[1]);
    return 0;

}

/* ========================================================================== */

/* Helper functions created by me by order of use */
/* ---------------------------------------------- */

/* Crawler function */
/* ---------------- */

/* Function that iterates through the list of urls and creates connections to 
 * each url, sends a GET header, and then parses through the data sent back */

void crawler(char* initial_url){

    int crawled = 0, added = 1;
    struct url url_list[250];
    
    // Split the first initial url and put it in the list
    url_list[crawled] = (struct url)categorise(initial_url);
    
    // Iterate through the urls in the list, crawling through each url and 
    // adding new urls to the list 
    while (crawled < MAX_URLS && crawled < added) {

        // Get the next url
        struct url current = url_list[crawled];

        // Print it
        printf("%s%s%s\n", current.host, current.path, current.file);

        // Create the socket
        int socket_id = create_socket(current.host);   

        char buffer[MAX_BYTES];
        bzero(buffer, MAX_BYTES); 
        char header[HEADER_SIZE];

        // Create the GET request
        GET_request(current, header);
        
        // Send the get request and read back what is sent back
        send(socket_id, header,strlen(header),0);
        read(socket_id, buffer, MAX_BYTES);
        
        // Parse throught the html and add all the hyperlinks encountered
        parse_html(buffer, url_list, &added);
        crawled++;

    }
}

/* -------------------------------------------------------------------------- */

/* Categorise function */
/* ------------------- */

/* Helper function that when given a URL, will give back a struct where its 
 * hostname, path, and file is separated */

struct url categorise(char* url) {

    struct url link;

    bzero(link.host, MAX_CHARACTERS);
    bzero(link.path, MAX_CHARACTERS);
    bzero(link.file, MAX_CHARACTERS);

    regex_t http_regex, path_regex, file_regex;
    regmatch_t http_rm[2], path_rm[2], file_rm[2];

    regcomp(&path_regex, PATH, REG_EXTENDED);
    regcomp(&http_regex, HTTP, REG_EXTENDED);
    regcomp(&file_regex, FILE, REG_EXTENDED);
    
    // If line starts with http:// then start after it

    if (regexec(&http_regex, url, 2, http_rm, 0) == 0) {
        url = &url[7];
    }
   
    //Get the host

    // If there's a path along with file and hostname
    if (regexec(&path_regex, url, 2, path_rm, 0) == 0) {
        
        
        int path_length = path_rm[0].rm_eo - path_rm[0].rm_so; 
        char* path = url + path_rm[0].rm_so;        
        char* file = url + path_rm[0].rm_eo;

        strcpy(link.file,file);    
        strncpy(link.path, path, path_length);
        strncpy(link.host,url, path_rm[0].rm_so);

    }
    else {
        
        // There's at least a file name
        if (regexec(&file_regex, url, 2, file_rm, 0) == 0) {
        
            char host[file_rm[0].rm_so];
            strncpy(host, url, file_rm[0].rm_so);
            char* file = url + file_rm[0].rm_so;
                
            strcpy(link.host, host);
            strcpy(link.path, "");
            strcpy(link.file, file);

        }

        // Is just the hostname, so just have / in the GET request
        else {

            strcpy(link.host, url);
            strcpy(link.path, "/");
            strcpy(link.file, "");   
        }
        
    }

    // Add in the null byte
    strcat(link.host, NULL_BYTE);
    strcat(link.path, NULL_BYTE);
    strcat(link.file, NULL_BYTE);

    regfree(&http_regex);
    regfree(&path_regex);
    regfree(&file_regex);

    return link;

}

/* -------------------------------------------------------------------------- */

/* Create socket function */
/* ------------------- */

/* Function mainly based off COMP30023 Lab 2 code to create a socket */

int create_socket(char *hostname){
    struct hostent * server;
    struct sockaddr_in serv_addr;
    int sockfd;

    
    server = gethostbyname(hostname);
    
    if (server == NULL) {
        fprintf(stderr, "ERROR, no such host\n");
        return -1;
    }

    bzero((char *)&serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy(server->h_addr_list[0], (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    serv_addr.sin_port = htons(PORT);

    sockfd = socket(AF_INET, SOCK_STREAM, 0);

    if (sockfd < 0) {
        perror("ERROR opening socket");
        return -1;
    }

    if (connect(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0){
        perror("ERROR connecting");
        return -1;
    }

    return sockfd;
}

/* -------------------------------------------------------------------------- */

/* GET request function */
/* ------------------- */

/* Function to create the GET function */

void GET_request(struct url url, char header[HEADER_SIZE]){

    bzero(header,HEADER_SIZE);

    strcpy(header,"GET ");
    strcat(header,url.path);
    strcat(header,url.file);
    strcat(header," HTTP/1.1\r\nHost: ");
    strcat(header, url.host);
    strcat(header,"\r\nUser-Agent: chanjieh\r\nConnection: keep-alive\r\n\r\n");

}

/* -------------------------------------------------------------------------- */

/* PArse HTML function */
/* ------------------- */

/* Function to parse through the data sent back from the connection and to 
 * append all the new urls that we need to fetch into the list */

void parse_html(char *buffer,struct url *url_list,int *added){

    regex_t anchor_regex, href_regex, end_regex, first_regex;
    regmatch_t anchor_rm[2], href_rm[2], end_rm[2], first_rm[2], first_b_rm[2];

    // Go through all the <a anchor tags
    regcomp(&anchor_regex, ANCHOR, REG_EXTENDED);
    
    while (regexec(&anchor_regex, buffer, 2, anchor_rm, 0) == 0) {

        // Find for the href attribute within the anchor tag
        buffer += anchor_rm[0].rm_eo;
        
        regcomp(&href_regex, HREF, REG_EXTENDED);
        regexec(&href_regex, buffer, 2, href_rm, 0);        

        // Extract the hyoerlink out of the href tag
        buffer += href_rm[0].rm_eo;
        
        regcomp(&end_regex, END_HREF, REG_EXTENDED);
        regexec(&end_regex, buffer, 2, end_rm, 0);

        int hyperlink_length = end_rm[0].rm_so;
        char hyperlink[hyperlink_length + 1];
        strncpy(hyperlink, buffer, hyperlink_length);
        strcat(hyperlink, NULL_BYTE);

        // Separate the hostname from its path and file name
        struct url url_link;
        url_link = categorise(hyperlink);
             

        // Check if similar enough to the original host without first component
        
        char* host = url_link.host;

        regcomp(&first_regex, FIRST_COMPONENT, REG_EXTENDED);
        regexec(&first_regex, host, 2, first_rm, 0);
        char* without_first = host + first_rm[0].rm_eo; 


        int found = 0; 

        for (int i = 0; i < *added; i++) {

            char* host_b = url_list[i].host;

            if (regexec(&first_regex, host_b, 2, first_b_rm, 0) == 0) {

                char* without_first_b = host_b + first_b_rm[0].rm_eo; 

                if (strcmp(host,host_b) == 0 || strcmp(without_first, without_first_b) == 0) { 

                    // Check if the page has already been visited before
                    
                    if ((strcmp(url_link.path, url_list[i].path) == 0 && strcmp(url_link.file, url_list[i].file) == 0)) {
                        
                        found += 1;

                    }   
                } 
            }
        }

        // Not visited before, so add to list

        if (found == 0) {
            
            url_list[*added] = url_link;
            *added += 1;
        }
    }
    
}


/* ========================================================================== */
