<template>
  <div class="home-container">
    <!-- Header with navigation -->
    <header class="app-header">
      <nav class="nav-menu">
        <div class="nav-left">
          <router-link to="/" class="nav-link">Главная</router-link>
        </div>
        <div class="nav-right">
          <div class="auth-buttons" v-if="!username">
            <router-link to="/login" class="auth-button login-button active">Вход</router-link>
            <router-link to="/register" class="auth-button register-button">Регистрация</router-link>
          </div>
          <div class="user-greeting" v-else>Добро пожаловать, {{ username }}</div>
        </div>
      </nav>
    </header>

    <main class="main-content auth-content">
      <div class="back-button-container">
        <button @click="$router.go(-1)" class="back-button">
          <i class="fas fa-arrow-left"></i> Назад
        </button>
      </div>

      <section class="auth-card">
        <h1 class="section-title">Вход в систему</h1>

        <form @submit.prevent="login" class="auth-form">
          <div class="form-group">
            <label for="username" class="form-label">Имя пользователя:</label>
            <input
                type="text"
                id="username"
                v-model="username"
                required
                class="form-input"
                placeholder="Введите ваш логин"
            >
          </div>

          <div class="form-group">
            <label for="password" class="form-label">Пароль:</label>
            <input
                type="password"
                id="password"
                v-model="password"
                required
                class="form-input"
                placeholder="Введите ваш пароль"
            >
          </div>

          <div class="submit-button-container">
            <button type="submit" class="auth-submit-button">
              Войти <i class="fas fa-sign-in-alt"></i>
            </button>
          </div>

          <div class="auth-links">
            <router-link to="/register" class="auth-link">
              Нет аккаунта? Зарегистрируйтесь
            </router-link>
          </div>

          <div v-if="errorMessage" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
          </div>
        </form>
      </section>
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <p>© 2023 Платформа компьютерного зрения. Все права защищены.</p>
    </footer>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
    };
  },
  methods: {
    async login() {
      try {
        const response = await axios.post(
            'http://localhost:8081/auth/login',
            {
              username: this.username,
              password: this.password,
            },
            {
              withCredentials: true,
            }
        );

        console.log('Server response:', response.data);
        console.log('Cookies:', document.cookie);

        this.$router.replace('/');
      } catch (error) {
        console.error('Login error:', error);
        this.errorMessage = error.response?.data?.message || 'Ошибка входа. Проверьте ваши данные.';
      }
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
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
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.nav-left {
  display: flex;
  align-items: center;
}

.nav-right {
  display: flex;
  align-items: center;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

.auth-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  min-height: calc(100vh - 120px);
}

.back-button-container {
  width: 100%;
  max-width: 500px;
  margin-bottom: 1rem;
}

.back-button {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius);
  transition: var(--transition);
}

.back-button:hover {
  background-color: rgba(67, 97, 238, 0.1);
}

.auth-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: var(--border-radius);
  padding: 2.5rem;
  box-shadow: var(--box-shadow);
  width: 100%;
  max-width: 500px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 2rem;
  color: var(--dark-color);
  text-align: center;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 1rem;
  color: var(--dark-color);
  font-weight: 500;
}

.form-input {
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: var(--border-radius);
  font-size: 1rem;
  transition: var(--transition);
  background-color: rgba(255, 255, 255, 0.8);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(67, 97, 238, 0.2);
}

.submit-button-container {
  margin-top: 1rem;
}

.auth-submit-button {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 1rem;
  border-radius: var(--border-radius);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
}

.auth-submit-button:hover {
  background-color: var(--secondary-color);
  transform: translateY(-2px);
}

.auth-links {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

.auth-link {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 0.9rem;
  transition: var(--transition);
}

.auth-link:hover {
  text-decoration: underline;
}

.error-message {
  color: var(--danger-color);
  background-color: rgba(220, 53, 69, 0.1);
  padding: 0.8rem;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.app-footer {
  background-color: var(--dark-color);
  color: white;
  text-align: center;
  padding: 1.5rem;
  margin-top: auto;
}

@media (max-width: 768px) {
  .auth-card {
    padding: 1.5rem;
  }

  .section-title {
    font-size: 1.5rem;
  }
}
</style>