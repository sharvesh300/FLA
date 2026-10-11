# FLA Project – Java Implementation

Five automata tasks: DFA simulation, NFA → DFA (subset construction), DFA minimization,
DFA intersection (product construction) and PDA simulation.

## Running

```bash
./gradlew test
./gradlew run --args="dfa examples/even-zeros.dfa 1001 ε"
./gradlew run --args="nfa2dfa examples/ends-with-ab.nfa"
./gradlew run --args="minimize examples/reducible.dfa"
./gradlew run --args="intersect examples/even-as.dfa examples/ends-with-b.dfa"
./gradlew run --args="pda examples/anbn.pda aabb aab"
./gradlew run
```

The last command opens an interactive menu.

## Automaton files

Lines starting with `#` are comments. `ε`, `eps` or `epsilon` stand for the empty symbol or string.

DFA and NFA:

```
states:   q0 q1 q2
alphabet: a b
start:    q0
accept:   q2
q0 a -> q0 q1
q1 b -> q2
q0 ε -> q2
```

A DFA allows exactly one target per transition and no `ε`. Missing transitions go to an implicit dead state.

PDA, with transitions written as `state input pop -> state push`:

```
states:        q0 q1
alphabet:      a b
stack:         Z A
start:         q0
initial-stack: Z
accept:        q1
accept-by:     final-state
q0 a Z -> q0 AZ
q0 ε Z -> q1 Z
```

Pushed strings are written top-first, so `AZ` leaves `A` on top. `accept-by` is `final-state` (default) or `empty-stack`.

## Layout

| Package      | Contents                                                         |
|--------------|------------------------------------------------------------------|
| `automata`   | Immutable `Dfa`, `Nfa`, `Pda` records with validation             |
| `algorithms` | `SubsetConstruction`, `DfaMinimizer`, `ProductConstruction`        |
| `simulation` | `DfaSimulator`, `PdaSimulator` with step-by-step traces            |
| `io`         | `AutomatonParser` for the file format, `AutomatonPrinter` for output |
