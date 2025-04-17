<template>
  <div class="yolo-model">
    <h1>YOLOv8 Object Detection</h1>

    <div class="upload-section">
      <input
        type="file"
        ref="fileInput"
        @change="handleFileUpload"
        accept=".png,.jpg,.jpeg,.mp4,.avi,.mov"
        class="file-input"
      />
      <button @click="uploadFile" :disabled="!file || isProcessing" class="upload-btn">
        {{ isProcessing ? 'Processing...' : 'Upload & Detect' }}
      </button>
    </div>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-if="result" class="results-section">
      <h2>Detection Results</h2>
      <p><strong>Filename:</strong> {{ result.filename }}</p>
      <p><strong>Type:</strong> {{ result.media_type }}</p>

      <div v-if="result.media_type === 'image'" class="image-container">
        <img :src="imagePreview" alt="Uploaded image" class="preview-image" />
        <div
          v-for="(detection, index) in result.detections"
          :key="index"
          class="bounding-box"
          :style="{
            left: `${(detection.bbox.x1 / imageWidth) * 100}%`,
            top: `${(detection.bbox.y1 / imageHeight) * 100}%`,
            width: `${((detection.bbox.x2 - detection.bbox.x1) / imageWidth) * 100}%`,
            height: `${((detection.bbox.y2 - detection.bbox.y1) / imageHeight) * 100}%`,
          }"
        >
          <span class="label">{{ detection.class }} ({{ (detection.confidence * 100).toFixed(1) }}%)</span>
        </div>
      </div>

      <div v-else class="video-container">
        <video controls class="preview-video">
          <source :src="videoPreview" :type="videoType" />
          Your browser does not support the video tag.
        </video>
      </div>

      <div class="detections-list">
        <h3>Detected Objects:</h3>
        <ul>
          <li v-for="(detection, index) in result.detections" :key="index">
            {{ detection.class }} - {{ (detection.confidence * 100).toFixed(1) }}% confidence
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'YoloModel',
  data() {
    return {
      file: null,
      isProcessing: false,
      error: null,
      result: null,
      imagePreview: null,
      videoPreview: null,
      videoType: null,
      imageWidth: 0,
      imageHeight: 0
    }
  },
  methods: {
    handleFileUpload(event) {
      this.file = event.target.files[0];
      this.error = null;
      this.result = null;

      if (this.file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          if (this.file.type.match('image.*')) {
            this.imagePreview = e.target.result;
            const img = new Image();
            img.onload = () => {
              this.imageWidth = img.width;
              this.imageHeight = img.height;
            };
            img.src = e.target.result;
          } else if (this.file.type.match('video.*')) {
            this.videoPreview = e.target.result;
            this.videoType = this.file.type;
          }
        };
        reader.readAsDataURL(this.file);
      }
    },

    async uploadFile() {
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

        this.result = response.data; // axios сам парсит JSON
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
/* ... (твои стили остались без изменений) ... */
</style>