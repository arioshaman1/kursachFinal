import requests
import pytest
from PIL import Image
import io
import os

# Настройки теста
BASE_URL = "http://localhost:5003"  # Базовый URL без /predict в конце
ENDPOINT = "/predict"  # Отдельно указываем endpoint


def test_horse_car_rabbit_classification():
    """Тест классификации лошади, машины и кролика"""

    test_images = [
        {
            "name": "лошадь.jpg",
            "expected_classes": ["horse", "mare", "stallion", "pony", "sorrel"],  # Добавили 'sorrel'
            "description": "Изображение лошади"
        },
        {
            "name": "машина.jpg",
            "expected_classes": ["car", "sports_car", "convertible", "limousine"],
            "description": "Изображение автомобиля"
        },
        {
            "name": "куян.jpg",  # кролик
            "expected_classes": ["hare", "rabbit", "bunny", "angora"],
            "description": "Изображение кролика"
        }
    ]

    for test_case in test_images:
        # Проверяем что файл существует
        assert os.path.exists(test_case["name"]), f"Файл {test_case['name']} не найден"

        with open(test_case["name"], "rb") as f:
            files = {"file": (test_case["name"], f, "image/jpeg")}
            response = requests.post(f"{BASE_URL}/predict", files=files)

            # Проверяем успешный запрос
            assert response.status_code == 200, f"Ошибка {response.status_code} для {test_case['name']}"

            result = response.json()
            predictions = [p["label"] for p in result["predictions"]]
            top_prediction = result["top_prediction"]["label"]

            # Выводим информацию для отладки
            print(f"\n{test_case['description']} ({test_case['name']}):")
            print("Топ-5 предсказаний:")
            for pred in result["predictions"]:
                print(f"- {pred['label']}: {pred['confidence']:.2f}")

            # Проверяем что хотя бы один ожидаемый класс есть в предсказаниях
            found = any(
                expected.lower() in pred.lower()
                for expected in test_case["expected_classes"]
                for pred in predictions
            )

            assert found, (
                f"Для {test_case['name']} ожидался один из {test_case['expected_classes']}, "
                f"но получено {predictions}. Топ предсказание: {top_prediction}"
            )

            # Дополнительная проверка уверенности топ-предсказания
            assert result["top_prediction"]["confidence"] > 0.3, (
                f"Низкая уверенность ({result['top_prediction']['confidence']:.2f}) "
                f"для {test_case['name']}"
            )


if __name__ == "__main__":
    # Запуск теста без pytest (для удобства)
    print("Запуск теста классификации изображений...")
    test_horse_car_rabbit_classification()
    print("\nТест завершен успешно!")