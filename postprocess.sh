#!/bin/bash

grep Superstep foo | grep ms | tr -s '' | cut -d '=' -f 2
