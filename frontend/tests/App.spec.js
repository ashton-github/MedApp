import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import App from '../src/App.vue'
import api from '../src/services/api.js'

vi.mock('../src/services/api.js')

describe('App.vue', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("affiche le statut UP quand le backend répond", async () => {
    api.get.mockResolvedValueOnce({
      data: { status: 'UP', mongodb: 'UP', service: 'medapp-backend' }
    })

    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.text()).toContain('UP')
  })

  it("affiche un message d'erreur quand le backend est injoignable", async () => {
    api.get.mockRejectedValueOnce(new Error('Network Error'))

    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.text()).toContain('injoignable')
  })
})
