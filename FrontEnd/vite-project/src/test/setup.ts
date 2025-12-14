import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock window.confirm
global.window.confirm = vi.fn(() => true);

// Mock console methods to reduce noise in tests
global.console = {
  ...console,
  log: vi.fn(),
  debug: vi.fn(),
  info: vi.fn(),
  warn: vi.fn(),
  error: vi.fn(),
};

