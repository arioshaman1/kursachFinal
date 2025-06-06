<template>
  <div class="classification-container">
    <div class="classification-card">
      <div class="header">
        <h2 class="title">Классификатор  животоных и их породы</h2>
        <p class="subtitle">Использует MobileNetV2</p>
      </div>

      <div class="upload-section">
        <div class="file-upload-area" @click="triggerFileInput" @dragover.prevent @drop="handleDrop">
          <div v-if="!previewImage" class="upload-prompt">
            <i class="upload-icon">📁</i>
            <p>Нажмите или перетащите картинку животного</p>
          </div>
          <img v-else :src="previewImage" alt="Preview" class="image-preview" />
          <input
              type="file"
              id="file"
              ref="fileInput"
              accept="image/*"
              required
              @change="handleFileChange"
              class="file-input"
          />
        </div>

        <div class="file-info" v-if="selectedFile">
          <p><strong> Название файла:</strong> {{ selectedFile.name }}</p>
          <p><strong>Размер:</strong> {{ formatFileSize(selectedFile.size) }}</p>
          <button @click="clearSelection" class="clear-btn">Очистить</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="goBack" class="back-button">
          <i class="fas fa-arrow-left"></i> Назад
        </button>
        <button
            @click="classifyImage"
            class="classify-button"
            :disabled="!selectedFile || isProcessing"
        >
          <span v-if="!isProcessing">
            <i class="fas fa-brain"></i> ОПРЕДЕЛИТЬ!!!
          </span>
          <span v-else>
            <i class="fas fa-spinner fa-spin"></i> Определяем...
          </span>
        </button>
      </div>

      <div v-if="errorMessage" class="message error-message">
        <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
      </div>

      <div v-if="result" class="result-section">
        <h3 class="result-title">Результаты классификации</h3>
        <p class="filename">File: {{ result.filename }}</p>

        <div class="top-prediction">
          <h4>Наиболее вероятно:</h4>
          <div class="prediction-item highlight">
            <span class="prediction-label">{{ result.top_prediction.label }}</span>
            <span class="prediction-confidence">
              {{ (result.top_prediction.confidence * 100).toFixed(2) }}%
            </span>
            <div class="confidence-bar">
              <div class="bar-fill" :style="{ width: result.top_prediction.confidence * 100 + '%' }"></div>
            </div>
          </div>
        </div>

        <div class="other-predictions">
          <h4>Так же это может быть:</h4>
          <div
              v-for="(pred, index) in result.predictions.slice(1)"
              :key="index"
              class="prediction-item"
          >
            <span class="prediction-label">{{ pred.label }}</span>
            <span class="prediction-confidence">
              {{ (pred.confidence * 100).toFixed(2) }}%
            </span>
            <div class="confidence-bar">
              <div class="bar-fill" :style="{ width: pred.confidence * 100 + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { useRouter } from 'vue-router';

export default {
  data() {
    return {
      selectedFile: null,
      previewImage: null,
      errorMessage: '',
      result: null,
      isProcessing: false,
    };
  },
  setup() {
    const router = useRouter();
    return { router };
  },
  methods: {
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        this.processFile(file);
      }
    },
    handleDrop(event) {
      event.preventDefault();
      const file = event.dataTransfer.files[0];
      if (file && file.type.match('image.*')) {
        this.processFile(file);
      }
    },
    processFile(file) {
      this.selectedFile = file;
      this.errorMessage = '';
      this.result = null;

      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewImage = e.target.result;
      };
      reader.readAsDataURL(file);
    },
    clearSelection() {
      this.selectedFile = null;
      this.previewImage = null;
      this.result = null;
      this.$refs.fileInput.value = '';
    },
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    goBack() {
      this.router.go(-1);
    },
    async classifyImage() {
      if (!this.selectedFile) {
        this.errorMessage = 'Please select an image file first.';
        return;
      }

      this.isProcessing = true;
      this.errorMessage = '';

      const formData = new FormData();
      formData.append('file', this.selectedFile);

      try {
        const response = await axios.post(
            'http://localhost:8080/api/mobilenet/upload',
            formData,
            {
              headers: {'Content-Type': 'multipart/form-data'},
              withCredentials: true,
            }
        );

        // Обрабатываем ответ в новом формате
        this.result = response.data;
      } catch (error) {
        console.error('Classification error:', error);
        this.errorMessage = error.response?.data?.error ||
            'Failed to classify image. Please try again.';
        this.result = null;
      } finally {
        this.isProcessing = false;
      }
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

.classification-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  font-family: 'Poppins', sans-serif;
}

.classification-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 100%;
  max-width: 700px;
  transition: all 0.3s ease;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0;
}

.upload-section {
  margin-bottom: 25px;
}

.file-upload-area {
  border: 2px dashed #bdc3c7;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 15px;
  background-color: #f8f9fa;
  position: relative;
  overflow: hidden;
}

.file-upload-area:hover {
  border-color: #3498db;
  background-color: #f1f8fe;
}

.upload-prompt {
  color: #7f8c8d;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 15px;
  display: block;
}

.image-preview {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  display: block;
  margin: 0 auto;
}

.file-input {
  display: none;
}

.file-info {
  background: #f8f9fa;
  padding: 12px 15px;
  border-radius: 8px;
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.clear-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.clear-btn:hover {
  background: #c0392b;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  margin-bottom: 25px;
}

.back-button {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  flex: 1;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.back-button:hover {
  background: #7f8c8d;
}

.classify-button {
  background: #9b59b6;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  flex: 2;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.classify-button:hover:not(:disabled) {
  background: #8e44ad;
}

.classify-button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.message {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.error-message {
  background: #fdecea;
  color: #d32f2f;
  border: 1px solid #ef9a9a;
}

.result-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
  border-left: 5px solid #9b59b6;
}

.result-title {
  color: #2c3e50;
  font-size: 22px;
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: 600;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.filename {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 20px;
}

.top-prediction, .other-predictions {
  margin-bottom: 20px;
}

.top-prediction h4, .other-predictions h4 {
  color: #34495e;
  margin-bottom: 15px;
  font-size: 18px;
}

.prediction-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 6px;
  background: white;
}

.prediction-item.highlight {
  background: #f0e6f6;
  border-left: 3px solid #9b59b6;
}

.prediction-label {
  flex: 2;
  font-weight: 500;
  color: #2c3e50;
  text-transform: capitalize;
}

.prediction-confidence {
  flex: 1;
  text-align: right;
  font-weight: 600;
  color: #9b59b6;
  margin-right: 15px;
}

.confidence-bar {
  flex: 3;
  height: 8px;
  background: #ecf0f1;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #9b59b6, #8e44ad);
  transition: width 0.5s ease;
}

@media (max-width: 768px) {
  .classification-card {
    padding: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .back-button, .classify-button {
    width: 100%;
  }

  .prediction-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .prediction-confidence {
    align-self: flex-end;
    margin-top: 5px;
  }

  .confidence-bar {
    width: 100%;
    margin-top: 8px;
  }
}
</style>