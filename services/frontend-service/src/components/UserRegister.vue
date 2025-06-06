<template>
  <div class="home-container">
    <!-- Шапка с навигацией -->
    <header class="app-header">
      <nav class="nav-menu">
        <div class="nav-left">
          <router-link to="/" class="nav-link">Главная</router-link>
        </div>
        <div class="nav-right">
          <router-link to="/login" class="auth-button login-button">Вход</router-link>
          <router-link to="/register" class="auth-button register-button active">Регистрация</router-link>
        </div>
      </nav>
    </header>

    <!-- Основное содержимое -->
    <main class="main-content auth-content">
      <!-- Кнопка "Назад" -->
      <div class="back-button-container">
        <button @click="$router.go(-1)" class="back-button">
          <i class="fas fa-arrow-left"></i> Назад
        </button>
      </div>

      <!-- Карточка регистрации -->
      <section class="auth-card">
        <h1 class="section-title">Создать аккаунт</h1>

        <!-- Форма регистрации -->
        <form @submit.prevent="register" class="auth-form">
          <!-- Поле имени пользователя -->
          <div class="form-group">
            <label for="username" class="form-label">Имя пользователя:</label>
            <input
                type="text"
                id="username"
                v-model="username"
                required
                class="form-input"
                placeholder="Придумайте логин"
                minlength="3"
                maxlength="20"
            >
          </div>

          <!-- Поле email -->
          <div class="form-group">
            <label for="email" class="form-label">Email:</label>
            <input
                type="email"
                id="email"
                v-model="email"
                required
                class="form-input"
                placeholder="Ваш email"
            >
          </div>

          <!-- Поле пароля -->
          <div class="form-group">
            <label for="password" class="form-label">Пароль:</label>
            <input
                type="password"
                id="password"
                v-model="password"
                required
                class="form-input"
                placeholder="Придумайте пароль"
                minlength="6"
            >
          </div>

          <!-- Подтверждение пароля -->
          <div class="form-group">
            <label for="confirmPassword" class="form-label">Подтвердите пароль:</label>
            <input
                type="password"
                id="confirmPassword"
                v-model="confirmPassword"
                required
                class="form-input"
                placeholder="Повторите пароль"
                minlength="6"
            >
          </div>

          <!-- Кнопка регистрации -->
          <div class="submit-button-container">
            <button type="submit" class="auth-submit-button">
              <i class="fas fa-user-plus"></i> Зарегистрироваться
            </button>
          </div>

          <!-- Ссылка на вход -->
          <div class="auth-links">
            <router-link to="/login" class="auth-link">
              Уже есть аккаунт? Войти
            </router-link>
          </div>

          <!-- Сообщение об ошибке -->
          <div v-if="errorMessage" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
          </div>

          <!-- Сообщение об успехе -->
          <div v-if="successMessage" class="success-message">
            <i class="fas fa-check-circle"></i> {{ successMessage }}
          </div>
        </form>
      </section>
    </main>

    <!-- Подвал -->
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
      email: '',
      password: '',
      confirmPassword: '',
      errorMessage: '',
      successMessage: ''
    };
  },
  methods: {
    async register() {
      // Сброс сообщений
      this.errorMessage = '';
      this.successMessage = '';

      // Валидация паролей
      if (this.password !== this.confirmPassword) {
        this.errorMessage = 'Пароли не совпадают';
        return;
      }

      // Валидация длины пароля
      if (this.password.length < 6) {
        this.errorMessage = 'Пароль должен содержать минимум 6 символов';
        return;
      }

      try {
        // Отправка данных на сервер
        const response = await axios.post(
            'http://localhost:8081/auth/register',
            {
              username: this.username,
              email: this.email,
              password: this.password,
            }
        );

        // Обработка успешной регистрации
        console.log('Регистрация успешна:', response.data);
        this.successMessage = 'Регистрация прошла успешно! Перенаправляем на страницу входа...';

        // Перенаправление через 2 секунды
        setTimeout(() => {
          this.$router.push('/login');
        }, 2000);

      } catch (error) {
        // Обработка ошибок
        console.error('Ошибка регистрации:', error);

        if (error.response) {
          // Ошибки от сервера
          if (error.response.status === 409) {
            this.errorMessage = 'Пользователь с таким именем или email уже существует';
          } else {
            this.errorMessage = error.response.data?.message || 'Ошибка сервера';
          }
        } else {
          this.errorMessage = 'Не удалось подключиться к серверу';
        }
      }
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

/* Основные переменные */
:root {
  --primary-color: #4361ee;
  --secondary-color: #3f37c9;
  --success-color: #4bb543;
  --danger-color: #dc3545;
  --dark-color: #212529;
  --gray-color: #6c757d;
  --light-gray: #f8f9fa;
  --border-radius: 8px;
  --box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
}

/* Основная структура */
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--light-gray);
  font-family: 'Poppins', sans-serif;
}

/* Шапка */
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

.nav-left, .nav-right {
  display: flex;
  align-items: center;
}

/* Стили навигации */
.nav-link {
  text-decoration: none;
  color: var(--dark-color);
  font-weight: 500;
  padding: 0.5rem 1rem;
  transition: var(--transition);
}

.nav-link:hover {
  color: var(--primary-color);
}

/* Кнопки авторизации */
.auth-buttons {
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

.register-button.active {
  background-color: var(--secondary-color);
}

/* Основное содержимое */
.main-content {
  flex: 1;
  padding: 2rem;
}

.auth-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
}

/* Кнопка "Назад" */
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

/* Карточка регистрации */
.auth-card {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: var(--border-radius);
  padding: 2.5rem;
  box-shadow: var(--box-shadow);
  width: 100%;
  max-width: 500px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 2rem;
  color: var(--dark-color);
  text-align: center;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background-color: var(--primary-color);
}

/* Форма */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.95rem;
  color: var(--dark-color);
  font-weight: 500;
}

.form-input {
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: var(--border-radius);
  font-size: 1rem;
  transition: var(--transition);
  background-color: white;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(67, 97, 238, 0.2);
}

/* Кнопка отправки */
.submit-button-container {
  margin-top: 1.5rem;
}

.auth-submit-button {
  background-color: var(--success-color);
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
  background-color: #3a9a3a;
  transform: translateY(-2px);
}

/* Ссылки */
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

/* Сообщения */
.error-message {
  color: var(--danger-color);
  background-color: rgba(220, 53, 69, 0.1);
  padding: 0.8rem;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.success-message {
  color: var(--success-color);
  background-color: rgba(75, 181, 67, 0.1);
  padding: 0.8rem;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  font-size: 0.9rem;
}

/* Подвал */
.app-footer {
  background-color: var(--dark-color);
  color: white;
  text-align: center;
  padding: 1.5rem;
  margin-top: auto;
}

/* Адаптивность */
@media (max-width: 768px) {
  .auth-card {
    padding: 1.5rem;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .nav-menu {
    flex-direction: column;
    align-items: flex-start;
  }

  .auth-buttons {
    margin-top: 1rem;
    width: 100%;
    justify-content: space-between;
  }

  .auth-button {
    flex: 1;
    text-align: center;
  }
}
</style>