#!/bin/bash

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎮 31 Game CLI를 시작합니다..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 환경변수 확인
if [ -z "$PLAYER_PRIVATE_KEY" ]; then
    echo "⚠️  환경변수 PLAYER_PRIVATE_KEY가 설정되지 않았습니다."
    echo "ℹ️  프로그램 시작 후 Private Key를 수동으로 입력하거나,"
    echo "   다음 명령어로 환경변수를 설정하세요:"
    echo ""
    echo "   export PLAYER_PRIVATE_KEY=0x..."
    echo ""
else
    echo "✓ 환경변수 PLAYER_PRIVATE_KEY가 설정되어 있습니다."
    echo "  자동 로그인됩니다."
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# CLI 모드로 실행
./gradlew bootRun --args='--app.mode=cli --WEB_APP_TYPE=none' --console=plain

