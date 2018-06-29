Chronique Queue Example Code
============================

It's an example code for a talk (a Tool In Action) about the Java library [Chronicle Queue](https://github.com/OpenHFT/Chronicle-Queue) at Devoxx France 2016 ([video](https://www.youtube.com/watch?v=yXZahjrbhc0) | [slides](https://www.slideshare.net/ThierryAbalea/dbridez-les-performances-de-vos-applications-avec-chronicle-queue)). 

It's an event-sourced implementation of a ticket-master like ticket booking system. There are 2 micro-services (the web server & the "ticket booking" service). The communication between the 2 services is based on Chronicle Queue.

This project is the Chronicle Queue version of a previous project initiated by [Michael Barker](https://github.com/mikeb01). This code is a clone of https://github.com/mikeb01/ticketing It was initialy an example code from the [Disruptor talk at Devoxx
2011](http://www.parleys.com/play/514892290364bc17fc56c469/chapter0/related).
The original code was a demonstration of how to use the Disruptor.

Authors:
* [Riad Maouchi](https://github.com/riadmaouchi)
* [Thierry Abaléa](https://github.com/ThierryAbalea)

Many thanks to:

* [Michael Barker](https://github.com/mikeb01) for the initial application
* [Peter Lawrey](https://github.com/peter-lawrey) for his contributions to our Chronicle Queue version.
