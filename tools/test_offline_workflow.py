#!/usr/bin/env python3
"""Run the SQL Server integration suite against an explicitly selected test database."""
import os
from pathlib import Path
import shutil
import subprocess

root = Path(__file__).resolve().parents[1]
url = os.environ.get('DB_URL', '')
if 'databasename=hotelofflinetest_' not in url.lower():
    raise SystemExit('Set DB_URL to an isolated HotelOfflineTest_* database. Never use the live database.')
if not os.environ.get('DB_USER') or not os.environ.get('DB_PASSWORD'):
    raise SystemExit('Set DB_USER and DB_PASSWORD in this process environment.')
maven = shutil.which('mvn') or shutil.which('mvn.cmd')
if not maven:
    raise SystemExit('Install Maven and add mvn to PATH.')
classpath_file = root / 'HotelPage/target/offline-test-classpath.txt'
subprocess.run([maven, '-q', '-f', str(root / 'HotelPage/pom.xml'), 'test-compile',
                'dependency:build-classpath', '-Dmdep.outputFile=' + str(classpath_file)], check=True)
classpath = os.pathsep.join([str(root / 'HotelPage/target/classes'),
                             str(root / 'HotelPage/target/test-classes'),
                             classpath_file.read_text().strip()])
subprocess.run(['java', '-cp', classpath, 'dao.OfflineWorkflowIT'], check=True)
