import { Routes } from '@angular/router';
import { authGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'trades',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'trades',
    loadComponent: () => import('./trades/trade-list/trade-list.component').then(m => m.TradeListComponent)
  },
  {
    path: 'trades/create',
    loadComponent: () => import('./trades/create-trade/create-trade.component').then(m => m.CreateTradeComponent),
    canActivate: [authGuard]
  },
  {
    path: 'trades/:id',
    loadComponent: () => import('./trades/trade-detail/trade-detail.component').then(m => m.TradeDetailComponent)
  },
  {
    path: 'leaderboard',
    loadComponent: () => import('./leaderboard/leaderboard.component').then(m => m.LeaderboardComponent)
  },
  {
    path: 'trader/:userId',
    loadComponent: () => import('./trader-profile/trader-profile.component').then(m => m.TraderProfileComponent)
  },
  {
    path: 'prices',
    loadComponent: () => import('./live-prices/price-dashboard/price-dashboard.component').then(m => m.PriceDashboardComponent)
  },
  {
    path: '**',
    redirectTo: 'trades'
  }
];
