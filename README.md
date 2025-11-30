# Application Goal

Develop a modern mobile application for performing engineering calculations with result visualization and history storage.

## Main Functionality

• Basic and engineering calculations - support for trigonometric, logarithmic functions, and constants;  
• Programmer mode - working with various number systems;  
• Function graphing - visualization of mathematical expressions;  
• Calculation history - saving and viewing all operations;  
• Application settings - customization of the interface and behavior.

## Technology Stack

• Language: Kotlin;  
• Minimum Version: Android 6.0 (API 24);  
• Build: Gradle Kotlin DSL.

## Key Architectural Components

### UI Layer:

• ViewBinding - for type-safe access to view elements;  
• Navigation Component - for navigation between screens;  
• Fragments - as the main UI components;  
• Material Design - for a modern interface.

### Architecture Pattern (MVVM):

• ViewModel (lifecycle-viewmodel-ktx) - for managing UI data;  
• LiveData (lifecycle-livedata-ktx) - for reactive UI updates;  
• Lifecycle (lifecycle-runtime-ktx) - for lifecycle management.

### Data Layer:

• Room Database - for local storage of calculation history;  
• Kotlin Coroutines (via room-ktx) - for asynchronous operations;  
• KAPT - for generating Room code.

### Business Logic:

• Exp4j - a mathematical parser for evaluating expressions.

## Author

Pankratov A.A. IS-B22.
