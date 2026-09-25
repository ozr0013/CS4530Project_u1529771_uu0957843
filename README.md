# CS 4530 Group Project — Drawing App

## Team Members

| Team Member   | uID      |
| ------------- | -------- |
| Omar Rizwan   | u1529771 |
| Ahmed Ali | U0957843 |
| herman Sjaastad | u1670628|

## Project Overview

This repository contains our CS 4530 Group Project: an interactive Drawing App developed according to the provided client requirements.

Our goal is to build the application incrementally, beginning with a functional Phase 1 prototype. This README documents our initial design, planned user interface, task breakdown, implementation responsibilities, testing strategy, and development sequence.

---

# Phase 1 Project Plan

## 1. Prototype Goals

For Phase 1, our goal is to create the core structure of the Drawing App and establish a solid foundation for later phases.

The prototype will focus on:

* Creating the main drawing interface
* Providing a usable drawing canvas
* Implementing the required Phase 1 drawing interactions
* Creating controls for interacting with the canvas
* Establishing navigation and application structure
* Separating UI, drawing logic, and application state
* Adding unit tests for core non-UI functionality

---

# 2. Sketches and Wireframes

## Main Drawing Screen

```text
+----------------------------------------------------------+
|                      DRAWING APP                         |
+----------------------------------------------------------+
|                                                          |
|  Tools                                                   |
|  +--------+                                              |
|  | Select |                                              |
|  | Draw   |          DRAWING CANVAS                      |
|  | Erase  |                                              |
|  +--------+                                              |
|                                                          |
|                                                          |
|                                                          |
|                                                          |
+----------------------------------------------------------+
|  Undo   |   Redo   |   Clear   |   Other Controls       |
+----------------------------------------------------------+
```

The main screen contains the application's drawing canvas and the controls needed to interact with it.

### Navigation Flow

```text
              +-------------------+
              |   Launch Drawing  |
              |       App         |
              +---------+---------+
                        |
                        v
              +-------------------+
              |   Main Drawing    |
              |      Screen       |
              +---------+---------+
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
     Select Tool    Drawing Tool   Other Tools
          |             |             |
          +-------------+-------------+
                        |
                        v
                 Drawing Canvas
```

The application is designed around a primary workspace so users can access drawing functionality without unnecessary navigation between screens.

> Note: These wireframes represent our initial Phase 1 design. The interface may evolve as implementation and usability testing progress.

---

# 3. Planned Application Structure

We plan to separate the application into components responsible for the user interface, drawing behavior, and application state.

### Layouts / UI Components

| Component              | Purpose                                         |
| ---------------------- | ----------------------------------------------- |
| Main Drawing Layout    | Primary application screen                      |
| Drawing Canvas         | Area where users create and manipulate drawings |
| Tool Controls          | Allows the user to select drawing tools         |
| Action Controls        | Provides actions such as undo, redo, or clear   |
| Status / Feedback Area | Displays relevant application feedback          |

### Classes / Components

The exact class names may change as development progresses.

| Class / Component   | Responsibility                                      |
| ------------------- | --------------------------------------------------- |
| `MainActivity`      | Controls the primary application screen             |
| `DrawingView`       | Displays the canvas and handles drawing interaction |
| `DrawingModel`      | Stores drawing data and application state           |
| `DrawingTool`       | Represents the currently selected drawing tool      |
| `DrawingController` | Coordinates user input with drawing behavior        |
| `HistoryManager`    | Manages undo and redo operations                    |

### Important Functions

Potential functions required during Phase 1 include:

```text
handleTouchInput()
startDrawing()
updateDrawing()
finishDrawing()
selectTool()
clearCanvas()
undo()
redo()
renderDrawing()
```

Function names and responsibilities may be adjusted as the implementation develops.

---

# 4. Unit Testing Plan

We will create unit tests alongside the implementation rather than waiting until the end of Phase 1.

Planned tests include:

| Test                    | Purpose                                                      |
| ----------------------- | ------------------------------------------------------------ |
| Drawing model tests     | Verify drawing data is stored correctly                      |
| Tool selection tests    | Verify the active tool changes correctly                     |
| Drawing operation tests | Verify drawing operations modify the model correctly         |
| Clear operation test    | Verify clearing removes drawing data                         |
| Undo test               | Verify the previous operation can be restored                |
| Redo test               | Verify an undone operation can be reapplied                  |
| State tests             | Verify application state remains consistent after operations |

UI-specific behavior will also be manually tested on the target device/emulator.

---

# 5. Task Breakdown

Tasks are listed in their planned implementation order.

| Order | Task                                                  | Assigned To | Deliverable                   |
| ----: | ----------------------------------------------------- | ----------- | ----------------------------- |
|     1 | Review client requirements and finalize Phase 1 scope | All Members | Agreed Phase 1 requirements   |
|     2 | Create initial UI wireframes                          | Omar        | UI design and navigation flow |
|     3 | Create main application layout                        | Ahmed   | Main drawing screen           |
|     4 | Implement drawing canvas/view                         | Member 3    | Functional canvas             |
|     5 | Create drawing model and state representation         | Omar        | Drawing data model            |
|     6 | Implement user input and drawing behavior             | Member 3    | Canvas interaction            |
|     7 | Implement tool controls                               | Ahmed | Functional tool selection     |
|     8 | Implement drawing actions and state management        | Omar        | Drawing operations            |
|     9 | Write model and state unit tests                      | Omar        | Unit test suite               |
|    10 | Write drawing/tool behavior tests                     | Member 3    | Drawing tests                 |
|    11 | Perform UI and integration testing                    | Ahmed   | Verified UI behavior          |
|    12 | Integrate all Phase 1 components                      | All Members | Complete prototype            |
|    13 | Fix bugs and polish the prototype                     | All Members | Stable Phase 1 build          |
|    14 | Review Phase 1 requirements and documentation         | All Members | Final Phase 1 submission      |

---

# 6. Development Sequence

We plan to develop the application in the following sequence:

```text
Requirements
     |
     v
Wireframes
     |
     v
Application Layout
     |
     v
Drawing Model
     |
     v
Drawing Canvas
     |
     v
User Interaction
     |
     v
Tools & Controls
     |
     v
Unit Testing
     |
     v
Integration Testing
     |
     v
Bug Fixes & Polish
     |
     v
Phase 1 Prototype
```

Tasks that do not depend on each other may be developed simultaneously by different team members.

---

# 7. Team Workflow

We will use GitHub to coordinate development.

Each team member will:

* Work on assigned tasks
* Commit changes regularly
* Use descriptive commit messages
* Keep changes focused on the assigned task
* Review integration issues with the team
* Test changes before merging them into the main branch

The team will periodically review progress and adjust the plan if implementation requirements change.

---

# Phase 1 Deliverables

By the end of Phase 1, we expect to have:

* [ ] Main application layout
* [ ] Functional drawing canvas
* [ ] Core drawing interactions
* [ ] Required tool controls
* [ ] Application state/model
* [ ] Unit tests for core functionality
* [ ] Integrated Phase 1 prototype
* [ ] Updated project documentation

---

## Repository Information

**Course:** CS 4530
**Project:** Drawing App
**Phase:** Phase 1
**Institution:** University of Utah
