#!/bin/bash
find ./ -type f -iname '*.java' | while read FILE; do
    echo "${FILE}"
    
    cat license-header.txt > source.tmp
    cat "${FILE}" | sed -zE 's/^.*(package.*)$/\1/g' >> source.tmp
    mv source.tmp "${FILE}"
done
