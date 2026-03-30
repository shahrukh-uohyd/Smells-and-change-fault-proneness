# Replication Package: Multi-language Design Smells and Maintenance Outcomes

This repository contains the replication package for the paper:

> "Title of Your Paper"

It includes all data, scripts, and instructions required to reproduce the results presented in the study.

---

## Overview

This study investigates the association between multi-language design smells and software maintenance outcomes, specifically:

- Change-proneness
- Fault-proneness

The replication package provides:

- Smell detection codes
- Smell detection results
- Processed datasets for change and faults
- Statistical analysis scripts
- Reproducible results

---

## Repository Structure

Smells-and-change-fault-proneness/
├── Data/ # Smells detected, processed datasets for change and fault proneness for fisher exact test and logistic regression
├── Code/ # Java code for smell detection, jupyter notebook script for preprocessing and analysis
├── results/ # Results for change and fault proneness for both fisher exact test and logistic regression

## Reproducing the results

Step 1: Detect smells:
      1. Open DetectCodeSmellsAndAntiPatterns.java under Code/smell detector/mlssdd.
      2. Uncomment the line corresponding to the smell you want to detect and assign dir=”smellName” (e.g., TooMuchClusteringDetectionModified and dir=”TooMuchClustering”).
      3. Recompile
      4. Run DetectCodeSmellsAndAntiPatterns.java
  Each run will produce a CSV file under: /Results/<SmellName>/<ProjectName>.csv

Step 2: Create dataset and analyze:
    Run change_fault-proneness.ipynb under the Code/Change and fault proneness analysis.
