from flask import Flask, request, jsonify
from ultralytics import YOLO
import os
import uuid
from werkzeug.utils import secure_filename
from flask_cors import CORS  # Импортируем CORS

# Инициализация модели YOLO
model = YOLO("yolov8n.pt")

# Конфигурация
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'mp4', 'avi', 'mov'}

# Инициализация Flask приложения
app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

CORS(app, resources={r"/predict": {"origins": "http://localhost:3000"}})

def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


# Функция для обработки изображения/видео
def process_file(file_path):
    results = model(file_path)

    # Собираем результаты детекции
    detections = []
    for result in results:
        for box in result.boxes:
            detections.append({
                'class': result.names[int(box.cls)],
                'confidence': float(box.conf),
                'bbox': {
                    'x1': float(box.xyxy[0][0]),
                    'y1': float(box.xyxy[0][1]),
                    'x2': float(box.xyxy[0][2]),
                    'y2': float(box.xyxy[0][3])
                }
            })

    # Для видео возвращаем только первый кадр (можно адаптировать)
    return {
        'filename': os.path.basename(file_path),
        'detections': detections,
        'media_type': 'video' if file_path.lower().endswith(('.mp4', '.avi', '.mov')) else 'image'
    }


# Маршрут для обработки POST-запросов
@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    if not allowed_file(file.filename):
        return jsonify({'error': 'File type not allowed'}), 400

    # Генерируем уникальное имя файла
    filename = secure_filename(file.filename)
    unique_filename = f"{uuid.uuid4().hex}_{filename}"
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)

    try:
        # Сохраняем файл
        file.save(file_path)

        # Обрабатываем файл
        result = process_file(file_path)

        return jsonify(result)

    except Exception as e:
        return jsonify({'error': str(e)}), 500

    finally:
        # Удаляем временный файл
        if os.path.exists(file_path):
            os.remove(file_path)


# Запуск Flask приложения
if __name__ == '__main__':
    # Создаем папку для загрузок, если её нет
    os.makedirs(UPLOAD_FOLDER, exist_ok=True)
    app.run(host='0.0.0.0', port=5001, debug=True)