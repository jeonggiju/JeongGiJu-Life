FROM grafana/grafana:latest

# 프로비저닝(데이터소스/대시보드 자동등록) 파일 복사
COPY provisioning /etc/grafana/provisioning

# (선택) 대시보드 JSON을 이미지에 포함
COPY dashboards /var/lib/grafana/dashboards
