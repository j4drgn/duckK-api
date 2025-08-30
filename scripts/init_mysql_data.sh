#!/bin/bash

# MySQL 데이터베이스 초기화 스크립트
# 실행 방법: ./init_mysql_data.sh [사용자명] [비밀번호]

# 기본값 설정
MYSQL_USER=${1:-root}
MYSQL_PASSWORD=${2:-""}
PROD_DB_NAME="duckchat"
DEV_DB_NAME="duckchat_dev"
SCHEMA_FILE="../src/main/resources/schema.sql"
DATA_FILE="../src/main/resources/data-mysql.sql"

# 비밀번호가 있는 경우와 없는 경우 명령 구분
if [ -z "$MYSQL_PASSWORD" ]; then
    MYSQL_CMD="mysql -u$MYSQL_USER"
else
    MYSQL_CMD="mysql -u$MYSQL_USER -p$MYSQL_PASSWORD"
fi

echo "MySQL 데이터베이스 초기화 중..."

# 프로덕션 데이터베이스 스키마 적용
echo "프로덕션 데이터베이스($PROD_DB_NAME)에 스키마 적용 중..."
$MYSQL_CMD $PROD_DB_NAME < $SCHEMA_FILE
if [ $? -eq 0 ]; then
    echo "스키마가 성공적으로 적용되었습니다."
else
    echo "스키마 적용 중 오류가 발생했습니다."
    exit 1
fi

# 프로덕션 데이터베이스 데이터 적용
echo "프로덕션 데이터베이스($PROD_DB_NAME)에 샘플 데이터 적용 중..."
$MYSQL_CMD $PROD_DB_NAME < $DATA_FILE
if [ $? -eq 0 ]; then
    echo "샘플 데이터가 성공적으로 적용되었습니다."
else
    echo "샘플 데이터 적용 중 오류가 발생했습니다."
    exit 1
fi

# 개발 데이터베이스 스키마 적용
echo "개발 데이터베이스($DEV_DB_NAME)에 스키마 적용 중..."
$MYSQL_CMD $DEV_DB_NAME < $SCHEMA_FILE
if [ $? -eq 0 ]; then
    echo "스키마가 성공적으로 적용되었습니다."
else
    echo "스키마 적용 중 오류가 발생했습니다."
    exit 1
fi

# 개발 데이터베이스 데이터 적용
echo "개발 데이터베이스($DEV_DB_NAME)에 샘플 데이터 적용 중..."
$MYSQL_CMD $DEV_DB_NAME < $DATA_FILE
if [ $? -eq 0 ]; then
    echo "샘플 데이터가 성공적으로 적용되었습니다."
else
    echo "샘플 데이터 적용 중 오류가 발생했습니다."
    exit 1
fi

echo "MySQL 데이터베이스 초기화가 완료되었습니다."
