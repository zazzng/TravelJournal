#!/bin/bash
cd "/Users/jana/Documents/Fall 2025/ID. 30016/TravelJournal"
java -cp out tj.TJ 2>&1 | grep -E "\[DEBUG-EMOJI\].*curves|defineCmd|Page curves"
