# Digital Library Management System
A Java desktop application for managing a digital library.

## Overview
This project is a digital library management system built with Java and Swing.  
It allows users to manage books, authors, borrowers, borrowing and returning operations, waiting lists, and analytical reports through a simple desktop GUI.

The main goal of the project is to apply data structures and algorithms in a practical system, not only as theoretical concepts.

## Features
- Manage books, authors, and borrowers
- Borrow and return books
- Prevent invalid borrowing operations
- Manage waiting lists for unavailable books
- Give priority to graduating students in waiting queues
- Generate analytical reports
- Save and import library data
- Visualize the difference between BST and AVL Tree

## Data Structures and Concepts Used
- AVL Tree for efficient book management
- BST visualization for comparison
- Priority Queue for waiting lists
- HashMap for mapping each book to its own waiting queue
- OOP principles
- File I/O
- Java Swing GUI

## How It Works
Books are managed using an AVL Tree to keep searching, insertion, and deletion efficient even when books are inserted in sorted order.

When a book is unavailable, the borrower is added to a waiting list.  
Each book has its own waiting list, and Priority Queue is used to give priority to graduating students. If borrowers have the same priority level, the earlier request is served first.

The system also updates available copies dynamically when books are borrowed, returned, or when new copies are added.

## Technologies
- Java
- Java Swing
- FlatLaf
- AVL Tree
- BST
- Priority Queue
- HashMap

## Download
A Windows executable version is available in the Releases section.

## Author
Mohamad Alloush
