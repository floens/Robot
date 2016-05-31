USER=pi
HOST=IPHERE
DEST=/home/pi/row01/controller/

scp -rp . $USER@$HOST:$DEST

ssh -T $USER@$HOST << EOF
cd $DEST
sudo killall controller_server
make
EOF
