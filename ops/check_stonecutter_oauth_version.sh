#!/bin/bash

result=$(lein ancient :allow-snapshots | grep sxtonecutter-oauth)

if [ -n "$result" ]; then
    exit 1
fi