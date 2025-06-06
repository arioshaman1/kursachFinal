<template>
  <div class="yolo-container">
    <div class="yolo-card">
      <div class="header">
        <h1 class="title">YOLOv8 Object Detection</h1>
        <p class="subtitle">Detect objects in images and videos</p>
      </div>

      <div
          class="drop-zone"
          @dragover.prevent="dragOver"
          @dragleave="dragLeave"
          @drop.prevent="handleDrop"
          :class="{ 'drag-active': isDragging }"
          @click="triggerFileInput"
      >
        <div class="drop-content">
          <i class="fas fa-cloud-upload-alt upload-icon"></i>
          <p>Drag & drop your file here</p>
          <span class="or-text">or</span>
          <button class="browse-btn">Browse Files</button>
          <input
              type="file"
              ref="fileInput"
              @change="handleFileSelect"
              accept=".png,.jpg,.jpeg,.mp4,.avi,.mov"
              class="file-input"
          />
          <p class="file-types">Supported formats: JPG, PNG, MP4, AVI, MOV</p>
        </div>
      </div>

      <div class="file-info" v-if="file">
        <div class="file-details">
          <i :class="fileIcon"></i>
          <div>
            <p class="filename">{{ file.name }}</p>
            <p class="filesize">{{ formatFileSize(file.size) }}</p>
          </div>
        </div>
        <button @click="clearFile" class="clear-btn">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="action-buttons">
        <button @click="goBack" class="back-button">
          <i class="fas fa-arrow-left"></i> Back
        </button>
        <button
            @click="processFile"
            class="detect-button"
            :disabled="!file || isProcessing"
        >
          <span v-if="!isProcessing">
            <i class="fas fa-search"></i> Detect Objects
          </span>
          <span v-else>
            <i class="fas fa-spinner fa-spin"></i> Processing...
          </span>
        </button>
      </div>

      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-circle"></i> {{ error }}
      </div>

      <div v-if="result" class="results-section">
        <h2 class="results-title">Detection Results</h2>

        <div class="media-preview">
          <div v-if="result.media_type === 'image'" class="image-container">
            <img
                :src="mediaPreview"
                alt="Processed image"
                class="preview-image"
                ref="previewImage"
                @load="setImageDimensions"
            />
            <div
                v-for="(detection, index) in result.detections"
                :key="index"
                class="bounding-box"
                :style="getBoundingBoxStyle(detection)"
            >
              <span class="label">{{ detection.class }} ({{ (detection.confidence * 100).toFixed(1) }}%)</span>
            </div>
          </div>

          <div v-else class="video-container">
            <video controls class="preview-video">
              <source :src="mediaPreview" :type="mediaType" />
              Your browser does not support the video tag.
            </video>
          </div>
        </div>

        <div class="detections-summary">
          <h3>Detection Summary</h3>
          <div class="stats">
            <div class="stat-item">
              <span class="stat-value">{{ result.detections.length }}</span>
              <span class="stat-label">Objects Found</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ uniqueClasses.length }}</span>
              <span class="stat-label">Unique Classes</span>
            </div>
          </div>

          <div class="detections-list">
            <h4>Detected Objects:</h4>
            <ul>
              <li v-for="(cls, index) in uniqueClasses" :key="index">
                <span class="class-name">{{ cls }}</span>
                <span class="class-count">{{ classCounts[cls] }} detected</span>
              </li>
            </ul>
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
  name: 'YoloModel',
  setup() {
    const router = useRouter();
    return {router};
  },
  data() {
    return {
      file: null,
      isDragging: false,
      isProcessing: false,
      error: null,
      result: null,
      mediaPreview: null,
      mediaType: null,
      imageWidth: 0,
      imageHeight: 0
    }
  },
  computed: {
    fileIcon() {
      if (!this.file) return '';
      return this.file.type.match('image.*') ? 'fas fa-image' : 'fas fa-video';
    },
    uniqueClasses() {
      if (!this.result) return [];
      return [...new Set(this.result.detections.map(d => d.class))];
    },
    classCounts() {
      if (!this.result) return {};
      return this.result.detections.reduce((acc, detection) => {
        acc[detection.class] = (acc[detection.class] || 0) + 1;
        return acc;
      }, {});
    }
  },
  methods: {
    goBack() {
      this.router.go(-1);
    },
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    dragOver() {
      this.isDragging = true;
    },
    dragLeave() {
      this.isDragging = false;
    },
    handleDrop(e) {
      this.isDragging = false;
      const file = e.dataTransfer.files[0];
      if (file) {
        this.handleFile(file);
      }
    },
    handleFileSelect(e) {
      const file = e.target.files[0];
      if (file) {
        this.handleFile(file);
      }
    },
    handleFile(file) {
      if (!file.type.match(/(image.*|video.*)/)) {
        this.error = 'Please select an image or video file';
        return;
      }

      this.file = file;
      this.error = null;
      this.result = null;

      const reader = new FileReader();
      reader.onload = (e) => {
        this.mediaPreview = e.target.result;
        this.mediaType = file.type;
      };
      reader.readAsDataURL(file);
    },
    clearFile() {
      this.file = null;
      this.mediaPreview = null;
      this.result = null;
      this.error = null;
      this.$refs.fileInput.value = '';
    },
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    setImageDimensions() {
      if (this.$refs.previewImage) {
        this.imageWidth = this.$refs.previewImage.naturalWidth;
        this.imageHeight = this.$refs.previewImage.naturalHeight;
      }
    },
    getBoundingBoxStyle(detection) {
      if (!this.imageWidth || !this.imageHeight) return {};
      return {
        left: `${(detection.bbox.x1 / this.imageWidth) * 100}%`,
        top: `${(detection.bbox.y1 / this.imageHeight) * 100}%`,
        width: `${((detection.bbox.x2 - detection.bbox.x1) / this.imageWidth) * 100}%`,
        height: `${((detection.bbox.y2 - detection.bbox.y1) / this.imageHeight) * 100}%`,
      };
    },
    async processFile() {
      if (!this.file) {
        this.error = 'Please select a file first';
        return;
      }

      this.isProcessing = true;
      this.error = null;

      const formData = new FormData();
      formData.append('file', this.file);

      try {
        const response = await axios.post('http://localhost:8080/api/yolo/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          withCredentials: true,
        });

        this.result = response.data;
      } catch (err) {
        this.error = `Error processing file: ${err.response?.data?.message || err.message}`;
        console.error('API Error:', err);
      } finally {
        this.isProcessing = false;
      }
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

.yolo-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  font-family: 'Poppins', sans-serif;
}

.yolo-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 100%;
  max-width: 900px;
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

.drop-zone {
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

.drop-zone.drag-active {
  border-color: #3498db;
  background-color: #e8f4fc;
}

.drop-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 3rem;
  color: #3498db;
  margin-bottom: 1rem;
}

.or-text {
  color: #777;
  margin: 0.5rem 0;
}

.browse-btn {
  display: inline-block;
  padding: 0.8rem 1.5rem;
  background-color: #3498db;
  color: white;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  border: none;
  font-size: 1rem;
}

.browse-btn:hover {
  background-color: #2980b9;
}

.file-input {
  display: none;
}

.file-types {
  color: #95a5a6;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.file-info {
  background: #f8f9fa;
  padding: 12px 15px;
  border-radius: 8px;
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-details {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filename {
  font-weight: 500;
  margin: 0;
  color: #2c3e50;
}

.filesize {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin: 0;
}

.clear-btn {
  background: #e74c3c;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
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

.detect-button {
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

.detect-button:hover:not(:disabled) {
  background: #8e44ad;
}

.detect-button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.error-message {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fdecea;
  color: #d32f2f;
  border: 1px solid #ef9a9a;
}

.results-section {
  margin-top: 30px;
}

.results-title {
  color: #2c3e50;
  font-size: 22px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.media-preview {
  margin-bottom: 25px;
}

.image-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  display: block;
}

.preview-video {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  display: block;
  margin: 0 auto;
}

.bounding-box {
  position: absolute;
  border: 2px solid #e74c3c;
  background-color: rgba(231, 76, 60, 0.2);
  z-index: 1;
}

.label {
  position: absolute;
  bottom: 100%;
  left: 0;
  background-color: #e74c3c;
  color: white;
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 4px 4px 0 0;
  white-space: nowrap;
}

.detections-summary {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: white;
  border-radius: 8px;
  flex: 1;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #9b59b6;
  display: block;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

.detections-list {
  margin-top: 15px;
}

.detections-list h4 {
  color: #34495e;
  margin-bottom: 15px;
  font-size: 18px;
}

.detections-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.detections-list li {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.class-name {
  font-weight: 500;
  color: #2c3e50;
  text-transform: capitalize;
}

.class-count {
  color: #7f8c8d;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .yolo-card {
    padding: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .back-button, .detect-button {
    width: 100%;
  }

  .stats {
    flex-direction: column;
    gap: 10px;
  }
}
</style>