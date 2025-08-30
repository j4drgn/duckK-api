#!/bin/bash

# MySQL 데이터베이스 설정 스크립트
# 실행 방법: ./setup_mysql.sh [사용자명] [비밀번호]

# 기본값 설정
MYSQL_USER=${1:-root}
MYSQL_PASSWORD=${2:-""}
PROD_DB_NAME="duckchat"
DEV_DB_NAME="duckchat_dev"

# 비밀번호가 있는 경우와 없는 경우 명령 구분
if [ -z "$MYSQL_PASSWORD" ]; then
    MYSQL_CMD="mysql -u$MYSQL_USER"
else
    MYSQL_CMD="mysql -u$MYSQL_USER -p$MYSQL_PASSWORD"
fi

echo "MySQL 데이터베이스 생성 중..."

# 프로덕션 데이터베이스 생성
$MYSQL_CMD -e "CREATE DATABASE IF NOT EXISTS $PROD_DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if [ $? -eq 0 ]; then
    echo "$PROD_DB_NAME 데이터베이스가 성공적으로 생성되었습니다."
else
    echo "$PROD_DB_NAME 데이터베이스 생성 중 오류가 발생했습니다."
    exit 1
fi

# 개발 데이터베이스 생성
$MYSQL_CMD -e "CREATE DATABASE IF NOT EXISTS $DEV_DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if [ $? -eq 0 ]; then
    echo "$DEV_DB_NAME 데이터베이스가 성공적으로 생성되었습니다."
else
    echo "$DEV_DB_NAME 데이터베이스 생성 중 오류가 발생했습니다."
    exit 1
fi

# 사용자가 root가 아닌 경우 권한 부여
if [ "$MYSQL_USER" != "root" ]; then
    echo "사용자 $MYSQL_USER에게 데이터베이스 권한 부여 중..."
    
    # 프로덕션 데이터베이스 권한
    $MYSQL_CMD -e "GRANT ALL PRIVILEGES ON $PROD_DB_NAME.* TO '$MYSQL_USER'@'localhost';"
    
    # 개발 데이터베이스 권한
    $MYSQL_CMD -e "GRANT ALL PRIVILEGES ON $DEV_DB_NAME.* TO '$MYSQL_USER'@'localhost';"
    
    # 권한 적용
    $MYSQL_CMD -e "FLUSH PRIVILEGES;"
    
    echo "권한이 성공적으로 부여되었습니다."
fi

echo "MySQL 데이터베이스 설정이 완료되었습니다."
echo "애플리케이션 실행을 위해 다음 환경 변수를 설정하세요:"
echo "export MYSQL_HOST=localhost"
echo "export MYSQL_PORT=3306"
echo "export MYSQL_DATABASE=$PROD_DB_NAME"
echo "export MYSQL_USERNAME=$MYSQL_USER"
if [ -n "$MYSQL_PASSWORD" ]; then
    echo "export MYSQL_PASSWORD=$MYSQL_PASSWORD"
fi

echo ""
echo "개발 환경에서는 다음과 같이 설정하세요:"
echo "export MYSQL_DATABASE=$DEV_DB_NAME"
