<template>
  <div class="home-container">
    <!-- Header with navigation -->
    <header class="app-header">
      <nav class="nav-menu">
        <router-link to="/" class="nav-link">Главная</router-link>
        <div class="auth-buttons" v-if="!username">
          <router-link to="/login" class="auth-button login-button">Вход</router-link>
          <router-link to="/register" class="auth-button register-button">Регистрация</router-link>
        </div>
        <div class="user-greeting" v-else>Добро пожаловать, {{ username }}</div>
      </nav>
    </header>

    <main class="main-content">
      <!-- Hero section -->
      <section class="hero-section">
        <h1 class="hero-title">Платформа компьютерного зрения</h1>
        <p class="hero-subtitle">Используйте мощные модели анализа изображений</p>
      </section>

      <!-- Model selection cards -->
      <section class="models-section">
        <h2 class="section-title">Доступные модели</h2>
        <div class="model-grid">
          <!-- YOLO Model Card -->
          <div class="model-card" @click="$router.push('/yolomodel')">
            <div class="model-icon">
              <i class="fas fa-object-group"></i>
            </div>
            <h3 class="model-title">YOLO (Обнаружение объектов)</h3>
            <p class="model-description">
              Модель для обнаружения объектов на изображениях в реальном времени с выделением рамками.
            </p>
            <div class="model-button">
              Попробовать YOLO
              <i class="fas fa-arrow-right"></i>
            </div>
          </div>

          <!-- MobileNet Card -->
          <div class="model-card" @click="$router.push('/mobilenet')">
            <div class="model-icon">
              <i class="fas fa-tags"></i>
            </div>
            <h3 class="model-title">MobileNet (Классификация)</h3>
            <p class="model-description">
              Легкая модель для классификации изображений по 1000 различным категориям.
            </p>
            <div class="model-button">
              Попробовать MobileNet
              <i class="fas fa-arrow-right"></i>
            </div>
          </div>

          <!-- Cat/Dog Classifier Card -->
          <div class="model-card" @click="$router.push('/upload')">
            <div class="model-icon">
              <i class="fas fa-paw"></i>
            </div>
            <h3 class="model-title">Кошка или Собака</h3>
            <p class="model-description">
              Специализированная модель для определения является ли животное на фото кошкой или собакой.
            </p>
            <div class="model-button">
              Определить
              <i class="fas fa-arrow-right"></i>
            </div>
          </div>
        </div>
      </section>

      <!-- User photos section -->
      <section class="photos-section" v-if="photos.length > 0">
        <h2 class="section-title">Ваши последние загрузки</h2>
        <div class="photo-grid">
          <div v-for="(photo, index) in photos.slice(0, 4)" :key="index" class="photo-card">
            <div class="photo-preview">
              <img v-if="photo.fileUrl" :src="photo.fileUrl" alt="Preview" class="photo-image">
              <i v-else class="fas fa-image"></i>
            </div>
            <div class="photo-info">
              <h3>{{ photo.originalFileName }}</h3>
              <p>{{ formatFileSize(photo.fileSize) }} • {{ formatDate(photo.uploadTime) }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="empty-state" v-else>
        <div class="empty-content">
          <i class="fas fa-images"></i>
          <h3>Нет загруженных фото</h3>
          <p>Выберите модель для анализа изображений</p>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <p>© 2025 Платформа классификации изображений. Все права защищены.</p>
    </footer>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      username: '',
      photos: [],
    };
  },
  async created() {
    await this.fetchUserPhotos();
  },
  methods: {
    async fetchUserPhotos() {
      try {
        const response = await axios.get('http://localhost:8080/api/files/user-photos', {
          withCredentials: true,
        });

        if (response.data) {
          this.username = response.data.username;
          this.photos = response.data.photos.map(photo => ({
            ...photo,
            fileUrl: this.generatePhotoUrl(photo.fileName)
          }));
        }
      } catch (error) {
        console.error('Ошибка при загрузке фото:', error);
      }
    },
    generatePhotoUrl(fileName) {
      return `http://localhost:8080/api/files/${fileName}`;
    },
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Байт';
      const k = 1024;
      const sizes = ['Байт', 'КБ', 'МБ', 'ГБ'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    formatDate(timestamp) {
      return new Date(timestamp).toLocaleDateString('ru-RU');
    }
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

:root {
  --primary-color: #4361ee;
  --secondary-color: #3f37c9;
  --accent-color: #4cc9f0;
  --light-color: #f8f9fa;
  --dark-color: #212529;
  --gray-color: #6c757d;
  --success-color: #4bb543;
  --warning-color: #ffc107;
  --danger-color: #dc3545;
  --border-radius: 8px;
  --box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Poppins', sans-serif;
  background: url('https://images.unsplash.com/photo-1519681393784-d120267933ba') center/cover no-repeat fixed;
  min-height: 100vh;
  min-height: 100vh;

}

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-header {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  box-shadow: var(--box-shadow);
  padding: 1rem 2rem;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-menu {
  display: flex;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.nav-link {
  text-decoration: none;
  color: var(--gray-color);
  margin-right: 1.5rem;
  font-weight: 500;
  transition: var(--transition);
  padding: 0.5rem 0;
  position: relative;
}

.nav-link:hover {
  color: var(--primary-color);
}


.nav-link.router-link-exact-active {
  color: var(--primary-color);
}

.nav-link.router-link-exact-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--primary-color);
}

.user-greeting {
  margin-left: auto;
  font-weight: 500;
  color: var(--primary-color);
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.hero-section {
  text-align: center;
  padding: 3rem 0;
  margin-bottom: 2rem;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 1rem;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--gray-color);
  max-width: 700px;
  margin: 0 auto;
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 2rem;
  color: var(--dark-color);
  position: relative;
  padding-bottom: 0.5rem;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: var(--primary-color);
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.model-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: var(--border-radius);
  padding: 2rem;
  box-shadow: var(--box-shadow);
  transition: var(--transition);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.model-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.model-card.coming-soon {
  opacity: 0.8;
  background-color: rgba(248, 249, 250, 0.7);
}

.model-icon {
  font-size: 2.5rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.model-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--dark-color);
}

.model-description {
  color: var(--gray-color);
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.model-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
}

.model-button:hover {
  background-color: var(--secondary-color);
  transform: translateY(-2px);
}

.model-button i {
  margin-left: 0.5rem;
  font-size: 0.8rem;
}

.model-button:disabled {
  background-color: var(--gray-color);
  cursor: not-allowed;
  transform: none;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.photo-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--box-shadow);
  transition: var(--transition);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.photo-card:hover {
  transform: translateY(-3px);
}

.photo-preview {
  height: 120px;
  background-color: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-color);
  font-size: 2rem;
  overflow: hidden;
}
.auth-buttons {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.auth-button {
  padding: 8px 16px;
  border-radius: var(--border-radius);
  text-decoration: none;
  font-weight: 500;
  transition: var(--transition);
}

.login-button {
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.login-button:hover {
  background-color: rgba(67, 97, 238, 0.1);
}

.register-button {
  background-color: var(--primary-color);
  color: white;
}

.register-button:hover {
  background-color: var(--secondary-color);
}

/* Исправляем стили для кнопок в карточках */
.model-card {
  position: relative; /* Добавляем для правильного позиционирования */
  padding-bottom: 70px; /* Оставляем место для кнопки */
}

.model-button {
  position: absolute;
  bottom: 20px;
  left: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
}

.model-button:hover {
  background-color: var(--secondary-color);
  transform: translateY(-2px);
}

.model-button i {
  margin-left: 8px;
  font-size: 0.9rem;
}

.model-button:disabled {
  background-color: var(--gray-color);
  cursor: not-allowed;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-info {
  padding: 1rem;
}

.photo-info h3 {
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.photo-info p {
  font-size: 0.8rem;
  color: var(--gray-color);
}

.view-all-button {
  display: inline-flex;
  align-items: center;
  color: var(--primary-color);
  font-weight: 500;
  text-decoration: none;
  transition: var(--transition);
}

.view-all-button:hover {
  color: var(--secondary-color);
}

.view-all-button i {
  margin-left: 0.5rem;
  font-size: 0.8rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  text-align: center;
}

.empty-content i {
  font-size: 3rem;
  color: var(--gray-color);
  margin-bottom: 1rem;
}

.empty-content h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: var(--dark-color);
}

.empty-content p {
  color: var(--gray-color);
  margin-bottom: 1.5rem;
}

.upload-button {
  display: inline-block;
  background-color: var(--primary-color);
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: var(--border-radius);
  text-decoration: none;
  font-weight: 500;
  transition: var(--transition);
}

.upload-button:hover {
  background-color: var(--secondary-color);
}

.app-footer {
  background-color: var(--dark-color);
  color: white;
  text-align: center;
  padding: 1.5rem;
  margin-top: 2rem;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .model-grid {
    grid-template-columns: 1fr;
  }

  .nav-menu {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-greeting {
    margin-left: 0;
    margin-top: 1rem;
  }
}
   /* Добавляем новые стили для кнопок авторизации */
 .auth-buttons {
   margin-left: auto;
   display: flex;
   gap: 10px;
 }

.auth-button {
  padding: 8px 16px;
  border-radius: var(--border-radius);
  text-decoration: none;
  font-weight: 500;
  transition: var(--transition);
}

.login-button {
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.login-button:hover {
  background-color: rgba(67, 97, 238, 0.1);
}

.register-button {
  background-color: var(--primary-color);
  color: white;
}

.register-button:hover {
  background-color: var(--secondary-color);
}

/* Исправляем стили для кнопок в карточках */
.model-button-wrapper {
  margin-top: auto;
  padding-top: 15px;
}

.model-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
  width: 100%;
}

.model-button:hover {
  background-color: var(--secondary-color);
}

.model-button i {
  margin-left: 0.5rem;
  font-size: 0.8rem;
}

.model-button:disabled {
  background-color: var(--gray-color);
  cursor: not-allowed;
}

/* Делаем всю карточку кликабельной */
.model-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
}
</style>