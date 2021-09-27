import React, { ComponentProps } from 'react';
import { Dashboard } from '@app/Dashboard/Dashboard';
import { Story } from '@storybook/react';

//👇 This default export determines where your story goes in the story list
export default {
  title: 'Components/Dashboard',
  component: Dashboard,
};

//👇 We create a “template” of how args map to rendering
const Template: Story<ComponentProps<typeof Dashboard>> = (args) => <Dashboard {...args} />;

export const FirstStory = Template.bind({});
FirstStory.args = {
  /*👇 The args you need here will depend on your component */
};
