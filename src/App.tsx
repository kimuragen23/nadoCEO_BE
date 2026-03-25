/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ChatPage } from './pages/chat/ChatPage';
import { LearningPathPage } from './pages/learning-path/LearningPathPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/course/java-basic/chat" replace />} />
        <Route path="/course/:courseId/chat" element={<ChatPage />} />
        <Route path="/learning-path/:sessionId" element={<LearningPathPage />} />
      </Routes>
    </BrowserRouter>
  );
}
