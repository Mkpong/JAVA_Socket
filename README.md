# Crepay ML Guide

### 1. Overview

사용자 특성을 통해 대안 신용도를 예측하기 위한 ML 모델

### 2. Before Using

1. 프로그램 사용 환경
- Ubuntu 20.04
- Python 3.9.19

1. Anaconda3 Virtual Enviroment Setup

```bash
conda create --name Crepay python=3.9 # 가상환경 생성
conda activate Crepay                 # 가상환경 실행
pip install -r requirement.txt        # 패키지 설치
```

### 3. Create Dataset

**1. File Format**

- **featurevector_data.tsv**

| 테스크 타입 | 수행 횟수 | 보상 평균 | 설명 | 작업 형태 | F1 | … | Fn |
| --- | --- | --- | --- | --- | --- | --- | --- |
- **repayment_data.tsv**

| 사용자 ID | 할부 ID | 할부 시작일 | 회차 | 혜정 상환일 | 상환 예정 원금 | 상환 예정 이자 | 상환일 | 예정일과의 차이 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 상환 ID | 상환 원금 | 상환 이자 | 잔여 원금 | 잔여 이자 | 스코어 |  |  |  |

→ 모두 한 Row에 있는 데이터

- **campaign_completed_transaction.tsv**

| id | user_id | campaign_id | task_type | created_time | provider | provider_reward_id | os_type |
| --- | --- | --- | --- | --- | --- | --- | --- |

**2. create_Dataset.py**

```bash
python create_dataset.py --task_feature_file_path ../data/featurevector_data.tsv \
												 --campaign_file_path ../data/campaign_completed_transaction.tsv \
												 --repayment_file_path ../data/repayment_data.tsv \
												 --feature_count 6 \
												 --output_file_path ../data/dataset.csv \
												 --score_type normal
```

**Argument**

| Args | **Explanation** | Default |
| --- | --- | --- |
| task_feature_file_path | Task Feature 파일 | ../data/featurevector_data.tsv |
| campaign_file_path | 사용자 Campaign 수행 데이터 파일 | ../data/campaign_completed_transaction.tsv |
| repayment_file_path | 상환 이력 테이블 파일 | ../data/repayment_data.tsv |
| feature_count | 정의된 feature 수 | 6 |
| output_file_path | dataset 파일 출력 경로 | ../data/dataset.csv |
| score_type | Score 계산 방법(exp, normal) | normal |

→ Score_type : exp → 지수 가중 평균 / normal → 단순 산술 평균

**Output**

![image.png](image.png)

→ Feature 수에 따라 user_id, X1~XN, score 출력

### 4. Find Best Parameter

```bash
python hparam_GS_lgbm.py --dataset_file_path ../data/dataset.csv \ 
                         --output_file_path ../data/hparam_lgbm.csv
```

**Argument**

| Args | **Explanation** | Default |
| --- | --- | --- |
| dataset_file_path | 학습에 사용할 dataset 경로 | ../data/dataset.csv |
| output_file_path | Parameter file 저장 경로 | ../data/hparam_lgbm.csv |

**Output**

![image.png](image%201.png)

→ Train에 사용할 하이퍼 파라미터

### 5. Train Model

```bash
python train_lgbm_kfold.py --dataset_file_path ../data/dataset.csv \
                           --model_version_or_name lgbm_kfold \
                           --hparam_file_path ../data/hparam_lgbm.csv
```

**Argument**

| Args | **Explanation** | Default |
| --- | --- | --- |
| dataset_file_path | 학습에 사용할 dataset 경로 | ../data/dataset.csv |
| model_version_or_name | 저장할 모델 이름(또는 버전) | lgbm_kfold |
| hparam_file_path | 불러올 Parameter file 경로 | ../data/hparam_lgbm.csv |

**Output**

- {model_version_or_name}.pkl : 학습된 모델 파일
- {model_version_or_name}_scaler.pkl : 학습시 사용한 scaler 파일
- {model_version_or_name}_log.txt

![image.png](image%202.png)

→ 모델의 성능 지표로 MSE, MAE, R^2 사용

- {model_version_or_name}_predictions.csv

![image.png](image%203.png)

→ 8:2로 분할하여 20% Data로 Test한 예측값, 실제값 저장

### 6. Sample API Call

**URL**

```python
http://220.149.232.224:5001/predict
```

**CURL Test Example**

→ 아래와 같이 데이터를 Body에 함께 넣어주면 

```bash
curl --location 'http://220.149.232.224:5001/predict' \
--header 'Content-Type: text/plain' \
--data 'NAVER_PLACE_SAVE	1601728
CREPAY_WEB_VIEW__NAVER_PLACE_SAVE	499697
CREPAY_WEB_VIEW__NAVER_PLACE_SEARCH_AND_SAVE	238135
CREPAY_ANSWER__NAVER_PLACE_TRAFFIC	231392
NAVER_STORE_PRODUCT_CLICK	189538
NAVER_PLACE_TRAFFIC	168203
QUESTION_AND_ANSWER	97139
KAKAO_PRODUCT_FAVORITE	85092
COUPANG_LAUNCH	27757
YOUTUBE_SUBSCRIBE	15879
INSTAGRAM_FOLLOW	14510
CREPAY_ANSWER__NAVER_STORE_PRODUCT_TRAFFIC	12359
NAVER_STORE_ALARM	12240
KAKAO_TALK_CHANNEL_JOIN	11316'
```

**Run app.py**

```python
nohup python app.py &
```

### 7. Result

- 날짜, 데이터별 점수 기록
