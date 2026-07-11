<script setup>
import { Bell, Moon, Sun, Search } from 'lucide-vue-next'
</script>

<template>
  <header class="h-16 flex items-center px-8 border-b border-sidebar-border bg-sidebar/50 backdrop-blur-md sticky top-0 z-40 shrink-0">
    <div class="flex-1 flex items-center">
      <div class="relative w-96 max-w-full">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <input
          type="search"
          placeholder="Rechercher (patients, ordonnances...)"
          class="w-full h-10 pl-9 pr-4 bg-background border border-sidebar-border rounded-xl text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition-all"
        />
      </div>
    </div>
    
    <div class="flex items-center gap-2">
      <button class="relative p-2 text-muted-foreground hover:bg-sidebar-accent hover:text-foreground rounded-xl transition-colors">
        <Bell class="w-5 h-5" />
        <span class="absolute top-2 right-2.5 w-2 h-2 bg-red-500 border-2 border-sidebar rounded-full"></span>
      </button>
      
      <button class="p-2 text-muted-foreground hover:bg-sidebar-accent hover:text-foreground rounded-xl transition-colors">
        <Sun class="w-5 h-5" />
      </button>
    </div>
  </header>
</template>