echo "--------------- 서버 배포 시작 -----------------"
docker stop jeonggiju-life-server || true
docker rm jeonggiju-life-server || true
docker pull 624545858643.dkr.ecr.ap-northeast-2.amazonaws.com/jeonggiju/life:latest
docker run -d --name jeonggiju-life-server -p 8080:8080 624545858643.dkr.ecr.ap-northeast-2.amazonaws.com/jeonggiju/life:latest
echo "--------------- 서버 배포 끝 ------------------"