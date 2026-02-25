
## Repository Structure

```
AI-Assignment-CSL7610-g25ait2119/
├── question1/
│   ├── ManuscriptSorter.java      # 8-Puzzle search algorithm solver
│   └── resource
│          └──input.txt            # Puzzle test cases (initial & goal states)
├── question2/
│   ├── SecurityBotCSP.java        # Security Bot CSP scheduler
│   └── resource
│          └──input.txt            # Bot/slot definitions and constraints
└── README.md
```

---

## Question 1 — 8-Puzzle Solver

### Problem

Solve the classic 8-puzzle problem using multiple search algorithms — BFS, DFS, A* (Manhattan + Misplaced Tiles), IDA*, and Simulated Annealing. The program reads puzzle configurations from `input.txt` and reports the solution path, nodes expanded, and execution time for each algorithm.

### Input Format (`question1/resource/input.txt`)

Each test case is a pair of lines — the initial state followed by the goal state. The puzzle is represented as a 3×3 grid with `0` denoting the blank tile.

```
1 2 3 4 0 5 6 7 8
1 2 3 4 5 6 7 8 0
```

### How to Run

#### Option A — GitHub Codespaces

1. Open the repository on GitHub and click **Code → Codespaces → Create codespace on main**.

2. Once the Codespace terminal loads, run:
   ```bash
   cd question1
   javac *.java
   ```
   To print the output in output.txt with intermediate states trace use below command
```bash
   java ManuscriptSorter resource/input.txt yes > resource/output.txt
   ```
   To print the output in output.txt with only start state and goal state and without states trace use below command
```bash
   java ManuscriptSorter resource/input.txt no > resource/output.txt
   ```
   To print the output in output.txt with intermediate states trace use below command
```bash
   java ManuscriptSorter resource/input.txt yes 
   ```
   To print the output in output.txt with only start state and goal state and without states trace use below command
```bash
   java ManuscriptSorter resource/input.txt no 
   ```

3. The program reads from `input.txt` in the same directory and prints results to the console.

#### Option B — Local Machine

**Prerequisites:** Java JDK 11 or higher installed. Verify with:
```bash
java -version
javac -version
```

**Steps:**
```bash
git clone https://github.com/g25ait2119/AI-Assignment-CSL7610-g25ait2119.git
cd AI-Assignment-CSL7610-g25ait2119/question1
javac *.java
```
OR
unzip the source code zip file AI-Assignment-CSL7610-g25ait2119.zip
```bash
cd AI-Assignment-CSL7610-g25ait2119/question1
javac *.java
```

To print the output in output.txt with intermediate states trace use below command
```bash
   java ManuscriptSorter resource/input.txt yes > resource/output.txt
   ```
To print the output in output.txt with only start state and goal state and without states trace use below command
```bash
   java ManuscriptSorter resource/input.txt no > resource/output.txt
   ```
To print the output in output.txt with intermediate states trace use below command
```bash
   java ManuscriptSorter resource/input.txt yes 
   ```
To print the output in output.txt with only start state and goal state and without states trace use below command
```bash
   java ManuscriptSorter resource/input.txt no 
   ```

> **Note:** Make sure `input.txt` is present in the same directory as the `.java` file before running. To use a custom input file, replace the contents of `input.txt` with your own test cases following the format shown above.

---

## Question 2 — Security Bot CSP Scheduler

### Problem

Assign 3 security bots (A, B, C) to 4 time slots using a Constraint Satisfaction Problem (CSP) solver. The solver uses Backtracking with MRV (Minimum Remaining Values) heuristic and Forward Checking.

**Constraints enforced:**
1. **No Back-to-Back** — A bot cannot work two consecutive slots.
2. **Maintenance Break** — Bot C cannot be assigned to Slot 4.
3. **Minimum Coverage** — Every bot must appear at least once.

### Input Format (`question2/input.txt`)

```
Bots: A, B, C
Slots: 1, 2, 3, 4
Constraint: NoBackToBack
Constraint: MaintenanceBreak C 4
Constraint: MinimumCoverage
```

### How to Run

#### Option A — GitHub Codespaces

1. Open the repository on GitHub and click **Code → Codespaces → Create codespace on main**.

2. Once the Codespace terminal loads, run:
   ```bash
   cd question2
   javac SecurityBotCSP.java
   java SecurityBotCSP
   ```

3. The solver prints the step-by-step MRV trace, forward checking inferences, final schedule, constraint verification, and performance metrics.

#### Option B — Local Machine

**Prerequisites:** Java JDK 11 or higher installed.

**Steps:**
```bash
git clone https://github.com/g25ait2119/AI-Assignment-CSL7610-g25ait2119.git
cd AI-Assignment-CSL7610-g25ait2119/question2
javac SecurityBotCSP.java
java SecurityBotCSP
```

> **Note:** The program reads from `input.txt` in the current working directory. You can also pass a custom file path as an argument:
> ```bash
> java SecurityBotCSP path/to/custom_input.txt
> ```

### Sample Output

```
Loaded CSP from: input.txt
Bots: [A, B, C] | Slots: 4
Starting backtracking search...

[MRV] Selected Slot1 (remaining values: {A, B, C})
  Assign Slot1 = A => OK
    -> Forward Check: pruned 'A' from Slot2's domain
[MRV] Selected Slot2 (remaining values: {B, C})
  Assign Slot2 = B => OK
    -> Forward Check: pruned 'B' from Slot3's domain
...

=============================================
  RESULT: Solution Found!
=============================================

  Final Bot Schedule:
    Slot 1  -->  Bot A
    Slot 2  -->  Bot B
    Slot 3  -->  Bot C
    Slot 4  -->  Bot A

  Constraint Checks:
    [PASS] No Back-to-Back
    [PASS] Maintenance Break
    [PASS] Minimum Coverage (all bots used)

---------------------------------------------
  Solver Statistics:
    Method       : Backtracking + MRV + Forward Checking
    Assignments  : 7
    Backtracks   : 2
    FC Inferences: 4
    Elapsed Time : 0.014153 sec
---------------------------------------------
```