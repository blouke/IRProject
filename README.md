## Information Retrieval Project
Information retrieval system over a small set of crawled web pages. (DePaul CSC-575 course project)

**Abstract**

Apache Nutch is used to crawl links obtained from DMOZ data. The crawled data is pre-processed using Apache Lucene tokenizer and filters, resulting terms are used to build vector-space index. The application is setup as a Java web application using Apache Maven and packaged into a WAR file. Upon deployment, it is accessible at http://localhost:8080/irproject/. The application upon receiving a search query does the necessary pre-processing, then uses JWNL (Java WordNet library) to access the WordNet data for incorporating new terms for query expansion, then calculates the cosine similarity against the relevant documents and returns the sorted results to the user. The user is also provided the opportunity to submit relevance feedback for each of the relevant documents, which the application makes uses of in calculating both positive and negative feedback using Rocchio method, updates the query accordingly, generates new results to be displayed to the user. There are two separate projects, the ‘IRProject’ which is the actual project that accepts search queries and relevance feedback against a collection of crawled web pages, and the second project ‘IRProjectEval’ which is cloned from the original project and edited to do the evaluation over test data. The URLs used to crawl webpages obtained from DMOZ data has a topic of 'Top/Shopping/Clothing/Casual'. So the system may generate relevant documents for only those queries that contains words related to casual clothing, examples are:
> shirt, shoe, women, men, calvin klein, gap, scarf, ....

Both projects, IRProject and IRProjectEval are also deployed on an Amazon EC2 instance (with an Elastic IP), accessible at:  
[http://52.88.63.246/irproject/](http://52.88.63.246/irproject/)  
[http://52.88.63.246/irprojecteval/](http://52.88.63.246/irprojecteval/)

