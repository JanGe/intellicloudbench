#!/bin/sh

rm -rf minion-0.12/
tar -zxvf minion-0.12-src.tar.gz
cd minion-0.12/
mkdir build
cd build/
cmake -DQUICK=1 ..
make minion -j $NUM_CPU_JOBS
echo $? > ~/install-exit-status
cd ../..
rm -rf minion-0.12/build/CMakeFiles/

echo "#!/bin/sh
cd minion-0.12/
./build/minion-quick \$@ > \$LOG_FILE 2>&1
echo \$? > ~/test-exit-status" > minion
chmod +x minion
