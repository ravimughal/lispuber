const DashAPI = {
  _delay: ms => new Promise(r => setTimeout(r, ms)),

  async bookRide(payload) {
    await this._delay(1400);
    return {
      rideId: 'D-' + Math.random().toString(36).slice(2,6).toUpperCase(),
      driver: { name: 'Marcus Webb', rating: 4.97, vehicle: 'Tesla Model 3', plate: 'AB-123-CD' },
      eta: { economy: 4, comfort: 6, premium: 3 }[payload.type] ?? 4,
    };
  },

  async cancelRide() { await this._delay(400); return { ok: true }; },
};