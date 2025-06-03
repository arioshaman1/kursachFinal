from flask import Flask, request, jsonify
from tensorflow.keras.applications.mobilenet_v2 import MobileNetV2, preprocess_input, decode_predictions
from tensorflow.keras.preprocessing import image
import numpy as np
import os
import uuid
from werkzeug.utils import secure_filename
from flask_cors import CORS

# Инициализация модели
model = MobileNetV2(weights='imagenet')

# Конфигурация
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}

# Инициализация Flask приложения
app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Разрешаем CORS для всех доменов
CORS(app)


def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


def predict_image(img_path):
    img = image.load_img(img_path, target_size=(224, 224))
    img_array = image.img_to_array(img)
    img_array = np.expand_dims(img_array, axis=0)
    img_array = preprocess_input(img_array)

    preds = model.predict(img_array)
    # Декодируем предсказания (вернет список классов ImageNet)
    decoded = decode_predictions(preds, top=5)[0]  # Возвращаем топ-5 предсказаний
    return [(label, float(conf)) for (_, label, conf) in decoded]


@app.route('/predict', methods=['POST', 'OPTIONS'])
def predict():
    if request.method == 'OPTIONS':
        response = jsonify()
        response.headers.add("Access-Control-Allow-Origin", "*")
        response.headers.add("Access-Control-Allow-Headers", "Content-Type")
        response.headers.add("Access-Control-Allow-Methods", "POST")
        return response

    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    if not allowed_file(file.filename):
        return jsonify({'error': 'File type not allowed'}), 400

    filename = secure_filename(file.filename)
    unique_filename = f"{uuid.uuid4().hex}_{filename}"
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)

    try:
        file.save(file_path)
        predictions = predict_image(file_path)

        # Форматируем результат
        result = {
            'filename': filename,
            'predictions': [{'label': label, 'confidence': conf} for label, conf in predictions],
            'top_prediction': {
                'label': predictions[0][0],
                'confidence': float(predictions[0][1])
            }
        }

        response = jsonify(result)
        response.headers.add('Access-Control-Allow-Origin', '*')
        return response

    except Exception as e:
        return jsonify({'error': str(e)}), 500

    finally:
        if os.path.exists(file_path):
            os.remove(file_path)


if __name__ == '__main__':
    os.makedirs(UPLOAD_FOLDER, exist_ok=True)
    app.run(host='0.0.0.0', port=5003, debug=True)