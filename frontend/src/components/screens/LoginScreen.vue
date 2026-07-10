<script setup>
import { useMedAppState } from '../../composables/useMedAppState.js'

const { authForm, signIn } = useMedAppState()
</script>

<template>
  <section class="screen login-screen">
    <div class="login-card">
      <div class="login-header">
        <h2>MedApp</h2>
        <p>Connexion a votre espace</p>
      </div>

      <label>Email</label>
      <input v-model="authForm.email" type="email" placeholder="nom@medapp.fr" />

      <label>Mot de passe</label>
      <input v-model="authForm.password" type="password" placeholder="········" />

      <button class="primary" type="button" @click="signIn">Se connecter</button>

      <p class="helper">Pas encore de compte ? Creer un compte</p>
    </div>
  </section>
</template>