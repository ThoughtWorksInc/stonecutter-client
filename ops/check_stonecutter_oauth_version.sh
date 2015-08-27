#!/bin/bash

result=$(lein ancient :allow-snapshots | grep stonecutter-oauth)

if [ -n "$result" ]; then
    exit 1
fi