import React from 'react';

interface UserMessageProps {
  content: string;
}

export function UserMessage({ content }: UserMessageProps) {
  return (
    <div className="flex justify-end mb-6">
      <div className="bg-blue-600 text-white px-5 py-3.5 rounded-3xl rounded-br-sm max-w-[85%] lg:max-w-[80%] text-[15px] leading-relaxed shadow-sm">
        {content}
      </div>
    </div>
  );
}
