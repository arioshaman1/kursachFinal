<template>
  <div class="image-classifier">
    <h2>Image Classification with MobileNet</h2>
    <!-- File upload form -->
    <form @submit.prevent="classifyImage">
      <div class="file-input">
        <label for="file">Select an image:</label>
        <input
            type="file"
            id="file"
            ref="fileInput"
            accept="image/*"
            required
            @change="handleFileChange"
        />
      </div>
      <button type="submit" class="classify-button" :disabled="isLoading">
        {{ isLoading ? 'Processing...' : 'Classify Image' }}
      </button>
    </form>

    <!-- Error message -->
    <div v-if="errorMessage" class="message error">
      {{ errorMessage }}
    </div>

    <!-- Results display -->
    <div v-if="result" class="results">
      <h3>Classification Results:</h3>
      <div class="image-preview">
        <img :src="imagePreview" v-if="imagePreview" alt="Uploaded image preview">
      </div>
      <div class="top-prediction">
        <h4>Top Prediction:</h4>
        <p><strong>Label:</strong> {{ result.top_prediction.label }}</p>
        <p><strong>Confidence:</strong> {{ (result.top_prediction.confidence * 100).toFixed(2) }}%</p>
      </div>
      <div class="all-predictions">
        <h4>All Predictions:</h4>
        <ul>
          <li v-for="(pred, index) in result.predictions" :key="index">
            {{ pred.label }}: {{ (pred.confidence * 100).toFixed(2) }}%
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      errorMessage: '',
      result: null,
      imagePreview: null,
      isLoading: false
    };
  },
  methods: {
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        // Create preview of the selected image
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imagePreview = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },
    async classifyImage() {
      const file = this.$refs.fileInput.files[0];

      if (!file) {
        this.errorMessage = 'Please select an image file first.';
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';
      this.result = null;

      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await axios.post(
            'http://localhost:8080/api/mobilenet/upload',
            formData,
            {
              headers: {
                'Content-Type': 'multipart/form-data'
              },
              withCredentials: true
            }
        );

        console.log('Classification results:', response.data);
        this.result = response.data;
      } catch (error) {
        console.error('Classification error:', error);
        this.errorMessage = error.response?.data?.error ||
            'Failed to classify image. Please try again.';
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.image-classifier {
  max-width: 600px;
  margin: 30px auto;
  padding: 25px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 25px;
  text-align: center;
}

.file-input {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #34495e;
}

input[type="file"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background-color: #f8f9fa;
}

.classify-button {
  width: 100%;
  padding: 12px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.classify-button:hover {
  background-color: #2980b9;
}

.classify-button:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

.message {
  padding: 12px;
  margin-top: 20px;
  border-radius: 6px;
}

.message.error {
  background-color: #fdecea;
  color: #d32f2f;
  border: 1px solid #ef9a9a;
}

.results {
  margin-top: 25px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.results h3 {
  margin-top: 0;
  color: #2c3e50;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

.image-preview {
  margin: 15px 0;
  text-align: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.top-prediction {
  background-color: #e8f4fd;
  padding: 15px;
  border-radius: 6px;
  margin: 15px 0;
}

.top-prediction h4 {
  margin-top: 0;
  color: #2980b9;
}

.all-predictions {
  margin-top: 15px;
}

.all-predictions h4 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.all-predictions ul {
  list-style-type: none;
  padding: 0;
}

.all-predictions li {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.all-predictions li:last-child {
  border-bottom: none;
}
</style>